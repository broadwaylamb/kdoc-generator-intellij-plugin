import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$PROJECT_ROOT/src/test/testData")
class KDocGeneratorTest : LightPlatformCodeInsightTestCase() {
    override fun getTestDataPath() = "src/test/testData/"

    private fun doTest() {
        val testName = getTestName(true)
        configureByFile(testName + "_before.kt")
        executeAction(IdeActions.ACTION_EDITOR_ENTER)

        // FIXME: The action is executed in a background thread.
        //  Without this sleep the test execution may finish earlier than the action is actually performed.
        Thread.sleep(1000)

        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
        checkResultByFile(testName + "_after.kt")
    }

    fun testClass() {
        doTest()
    }

    fun testClassWithGeneric() {
        doTest()
    }

    fun testClassWithGenericAndProperties() {
        doTest()
    }

    fun testClassWithProperties() {
        doTest()
    }
    fun testEnumEntry() {
        doTest()
    }

    fun testFunction() {
        doTest()
    }

    fun testFunctionUnit() {
        doTest()
    }

    fun testFunctionWithGeneric() {
        doTest()
    }
}
