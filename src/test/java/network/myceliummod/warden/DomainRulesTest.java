package network.myceliummod.warden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DomainRulesTest {

    @Test
    public void testBuiltin() {
        DomainRules rules = DomainRules.builtin();
        Assertions.assertTrue(rules.size() > 0);
    }
}
