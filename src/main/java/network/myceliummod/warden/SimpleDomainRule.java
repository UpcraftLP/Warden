package network.myceliummod.warden;

public final class SimpleDomainRule implements DomainRule {

    private final String domain;
    private final String subdomain;

    public SimpleDomainRule(String domain) {
        this.domain = domain;
        // Matching subdomains was faster this way. This trades a bit of ram that will soon be freed
        // for a reduced execution time.
        this.subdomain = "." + domain;
    }

    @Override
    public boolean test(String domain) {
        return domain.equals(this.domain) || domain.endsWith(this.subdomain);
    }
}
