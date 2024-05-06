package network.myceliummod.warden;

import network.myceliummod.warden.main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DomainRulesTest {

    private static final Path TEST_DIR = Paths.get("test-files");

    @BeforeAll
    public static void setupFiles() throws IOException {
        Random random = new Random();

        Files.createDirectories(TEST_DIR);
        for (int i = 0; i < 1000; i++) {
            Path file = TEST_DIR.resolve("file" + i + ".txt");
            try (OutputStream stream = Files.newOutputStream(file)) {
                int r = random.nextInt(10);
                for (int j = 0; j < r; j++) {
                    byte[] data = new byte[1024];
                    random.nextBytes(data);
                    stream.write(data);
                }
            }

            UserDefinedFileAttributeView attributes = Files.getFileAttributeView(file, UserDefinedFileAttributeView.class);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                writer.println("[ZoneTransfer]");
                writer.println("ZoneId=3"); // downloaded from the internet


                // TODO generate these from a list of known hosts, and have non-matches as well
                switch (i % 3) {
                    case 0:
                        // no ADS
                        break;
                    case 1:
                        // host only
                        writer.println("HostUrl=https://9minecraft.net");
                        break;
                    case 2:
                        // host and referrer
                        writer.println("ReferrerUrl=https://dl5.9minecraft.net/index.php?act=dl&id=" + i);
                        writer.println("HostUrl=https://dl5.9minecraft.net/index.php?act=download&id=" + i);
                        break;
                }

                writer.flush();
                byte[] data = out.toByteArray();
                attributes.write("Zone.Identifier", ByteBuffer.wrap(data));
            }
        }
        System.out.println("Generated test files.");
    }

    @Test
    public void builtinRulesExist() {
        DomainRules rules = DomainRules.builtin();
        Assertions.assertTrue(rules.size() > 0, "Unable to load builtin rules.");
    }

    @Test
    public void checkFiles() {
        DomainRules rules = DomainRules.builtin();
        File testDir = TEST_DIR.toFile();

        List<Main.MatchResult> matches = new LinkedList<>();
        Assertions.assertTrue(Main.checkFiles(rules, testDir, matches), "No matching files were found.");
        Assertions.assertFalse(matches.isEmpty(), "Matching files is empty.");

        System.out.println("Found " + matches.size() + " matching files.");
    }
}
