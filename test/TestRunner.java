import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

public class TestRunner {
    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectPackage("unit"))
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        launcher.execute(request, listener);

        var summary = listener.getSummary();
        System.out.println();
        System.out.println("=== Test Results ===");
        System.out.println("Tests found: " + summary.getTestsFoundCount());
        System.out.println("Tests passed: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        System.out.println("Tests skipped: " + summary.getTestsSkippedCount());

        var failures = summary.getFailures();
        if (!failures.isEmpty()) {
            System.out.println();
            System.out.println("=== FAILURES ===");
            failures.forEach(failure -> {
                System.out.println("FAILED: " + failure.getTestIdentifier().getDisplayName());
                System.out.println("  " + failure.getException().getMessage());
            });
        }

        System.exit(summary.getTestsFailedCount() > 0 ? 1 : 0);
    }
}
