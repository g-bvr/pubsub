import org.jkube.gitbeaver.util.test.CreatePluginDocumentation;
import org.jkube.gitbeaver.util.test.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DocumentationTest extends CreatePluginDocumentation {

    @BeforeAll
    static void beforeAllTests() { TestUtil.beforeTests(); }

    @BeforeEach
    void beforeEachTest() { TestUtil.beforeEachTest(); }

    @Test
    void createDocumentation() {
        createDocu();
    }

    @AfterEach
    void afterEachTest() { TestUtil.assertNoFailures(); }


}
