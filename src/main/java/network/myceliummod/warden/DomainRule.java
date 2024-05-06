package network.myceliummod.warden;

import java.util.function.Predicate;

public interface DomainRule extends Predicate<String> {

    @Override
    boolean test(String domain);
}
