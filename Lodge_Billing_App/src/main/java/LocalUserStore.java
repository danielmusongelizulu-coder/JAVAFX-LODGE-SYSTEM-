import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

final class LocalUserStore {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Path accountFile = Path.of(
            System.getProperty("user.home"), ".muzu-lodge", "users.properties");

    Account authenticate(String username, char[] password) throws IOException, GeneralSecurityException {
        Properties users = load();
        String prefix = "user." + username.toLowerCase(Locale.ROOT) + ".";
        String encodedSalt = users.getProperty(prefix + "salt");
        String encodedHash = users.getProperty(prefix + "hash");
        if (encodedSalt == null || encodedHash == null) {
            return null;
        }

        byte[] salt = Base64.getDecoder().decode(encodedSalt);
        byte[] expectedHash = Base64.getDecoder().decode(encodedHash);
        byte[] actualHash = hashPassword(password, salt);
        if (!MessageDigest.isEqual(expectedHash, actualHash)) {
            return null;
        }
        return new Account(users.getProperty(prefix + "username", username),
                users.getProperty(prefix + "name", username));
    }

    List<Account> listAccounts() throws IOException {
        Properties users = load();
        Set<String> usernames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String key : users.stringPropertyNames()) {
            if (key.startsWith("user.") && key.endsWith(".hash")) {
                String username = key.substring("user.".length(), key.length() - ".hash".length());
                usernames.add(users.getProperty("user." + username + ".username", username));
            }
        }
        return usernames.stream()
                .map(username -> {
                    String prefix = "user." + username.toLowerCase(Locale.ROOT) + ".";
                    return new Account(username, users.getProperty(prefix + "name", username));
                })
                .sorted(Comparator.comparing(Account::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    void register(String username, char[] password)
            throws IOException, GeneralSecurityException {
        Properties users = load();
        String prefix = "user." + username.toLowerCase(Locale.ROOT) + ".";
        if (users.containsKey(prefix + "hash")) {
            throw new IllegalArgumentException("That username is already registered.");
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        users.setProperty(prefix + "username", username);
        users.setProperty(prefix + "name", username);
        users.setProperty(prefix + "salt", Base64.getEncoder().encodeToString(salt));
        users.setProperty(prefix + "hash",
                Base64.getEncoder().encodeToString(hashPassword(password, salt)));
        save(users);
    }

    void deleteAccount(String username) throws IOException {
        Properties users = load();
        String prefix = "user." + username.toLowerCase(Locale.ROOT) + ".";
        if (!users.containsKey(prefix + "hash")) {
            throw new IllegalArgumentException("That account no longer exists.");
        }
        for (String key : users.stringPropertyNames().stream()
                .filter(property -> property.startsWith(prefix)).toList()) {
            users.remove(key);
        }
        save(users);
    }

    private Properties load() throws IOException {
        Properties users = new Properties();
        if (Files.exists(accountFile)) {
            try (var input = Files.newInputStream(accountFile)) {
                users.load(input);
            }
        }
        return users;
    }

    private void save(Properties users) throws IOException {
        Files.createDirectories(accountFile.getParent());
        Path temporaryFile = Files.createTempFile(accountFile.getParent(), "users-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                users.store(output, "MUZU Lodge local user accounts");
            }
            try {
                Files.move(temporaryFile, accountFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporaryFile, accountFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private byte[] hashPassword(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec specification = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
        } finally {
            specification.clearPassword();
        }
    }

    record Account(String username, String legacyName) {
    }
}
