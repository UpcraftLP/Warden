package network.myceliummod.warden;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Set;

/**
 * Holds the rules used to match a domain. Currently, rules can specify a domain and all subdomains, or a subdomain and
 * all nested subdomains.
 */
public class DomainRules {

    private final Set<DomainRule> rules = new ObjectOpenHashSet<>();

    /**
     * Creates a new set of rules that contains a subset of the domains catalogued by the stop mod reposts org.
     *
     * @return A new set of the default rules.
     */
    public static DomainRules builtin() {
        final DomainRules rules = new DomainRules();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(DomainRules.class.getClassLoader().getResourceAsStream("builtin-domain-rules.txt"), "Failed to load builtin-domain-rules.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rules.addRule(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("unable to load builtin rules", e);
        }

        return rules;
    }

    public void addRule(String domain) {
        this.addRule(new SimpleDomainRule(domain));
    }

    public void addRule(DomainRule rule) {
        this.rules.add(rule);
    }

    /**
     * Checks if the host or referrer of an identifier is matched.
     *
     * @param identifier The zone identifier to test.
     * @return If the identifier matched a rule.
     */
    public boolean test(ZoneIdentifier identifier) {
        return identifier != null && (testDomain(identifier.getReferrer()) || testDomain(identifier.getHost()));
    }

    /**
     * Tests if a domain or subdomain matches a rule.
     *
     * @param domain The domain to test.
     * @return If the domain matched a rule.
     */
    public boolean testDomain(String domain) {
        if (domain != null) {
            for (DomainRule rule : this.rules) { // This was significantly faster than streams.
                if (rule.test(domain)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int size() {
        return rules.size();
    }
}
