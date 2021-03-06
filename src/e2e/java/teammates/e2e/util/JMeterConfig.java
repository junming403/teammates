package teammates.e2e.util;

import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.OnceOnlyController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.OnceOnlyControllerGui;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * JMeter configuration for an L&P test.
 */
public abstract class JMeterConfig {

    protected abstract int getNumberOfThreads();

    protected abstract int getRampUpPeriod();

    protected abstract String getTestEndpoint();

    protected abstract String getTestMethod();

    protected abstract Map<String, String> getRequestParameters();

    protected abstract String getCsvConfigPath();

    protected abstract String getRequestBody();

    protected abstract String getRequestBodyContentType();

    /**
     * Returns the JMeter {@code HashTree} object with some standard configurations
     * and some test-specific configurations.
     */
    public HashTree createTestPlan() {
        // Test Plan
        TestPlan testPlan = new TestPlan();
        testPlan.setName("L&P Test Plan");
        testPlan.setEnabled(true);
        testPlan.setUserDefinedVariables(new Arguments());
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());

        // Thread Group controller
        LoopController loopController = new LoopController();
        loopController.setEnabled(true);
        loopController.setLoops(1);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());

        // Thread Group
        ThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setName("Thread Group");
        threadGroup.setNumThreads(getNumberOfThreads());
        threadGroup.setRampUp(getRampUpPeriod());
        threadGroup.setProperty(new StringProperty(ThreadGroup.ON_SAMPLE_ERROR, ThreadGroup.ON_SAMPLE_ERROR_CONTINUE));
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

        // CSV Config
        CSVDataSet csvDataSet = new CSVDataSet();
        csvDataSet.setName("CSV Data Config");
        csvDataSet.setProperty(new StringProperty("filename", getCsvConfigPath()));
        csvDataSet.setProperty(new StringProperty("delimiter", "|"));
        csvDataSet.setProperty(new StringProperty("shareMode", "shareMode.all"));
        csvDataSet.setProperty("ignoreFirstLine", true);
        csvDataSet.setProperty("quoted", true);
        csvDataSet.setProperty("quotedData", true);
        csvDataSet.setProperty("recycle", false);
        csvDataSet.setProperty("stopThread", true);
        csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
        csvDataSet.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());

        // Cookie Manager
        CookieManager cookieManager = new CookieManager();
        cookieManager.setName("HTTP Cookie Manager");
        cookieManager.setClearEachIteration(false);
        cookieManager.setCookiePolicy("standard");
        cookieManager.setProperty(TestElement.TEST_CLASS, CookieManager.class.getName());
        cookieManager.setProperty(TestElement.GUI_CLASS, CookiePanel.class.getName());

        // HTTP Default Sampler
        ConfigTestElement defaultSampler = new ConfigTestElement();
        defaultSampler.setName("HTTP Request Defaults");
        defaultSampler.setEnabled(true);
        defaultSampler.setProperty(new TestElementProperty(HTTPSampler.ARGUMENTS, new Arguments()));
        defaultSampler.setProperty(HTTPSampler.DOMAIN, "localhost");
        defaultSampler.setProperty(HTTPSampler.PORT, "8080");
        defaultSampler.setProperty(TestElement.TEST_CLASS, ConfigTestElement.class.getName());
        defaultSampler.setProperty(TestElement.GUI_CLASS, HttpDefaultsGui.class.getName());

        // Login HTTP Request
        HTTPSamplerProxy loginSampler = new HTTPSamplerProxy();
        loginSampler.setName("Login");
        loginSampler.setPath(
                "_ah/login?action=Log+In&email=${email}&isAdmin=${isAdmin}&continue=http://localhost:8080/webapi/auth");
        loginSampler.setMethod("POST");
        loginSampler.setFollowRedirects(true);
        loginSampler.setUseKeepAlive(true);
        loginSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        loginSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // Controller for Login request
        OnceOnlyController onceOnlyController = new OnceOnlyController();
        onceOnlyController.setName("Once Only Login Controller");
        onceOnlyController.setProperty(TestElement.TEST_CLASS, OnceOnlyController.class.getName());
        onceOnlyController.setProperty(TestElement.GUI_CLASS, OnceOnlyControllerGui.class.getName());

        // Test API Endpoint HTTP Request
        HTTPSamplerProxy apiSampler = new HTTPSamplerProxy();
        apiSampler.setName("Test Endpoint");
        apiSampler.setPath(getTestEndpoint());
        apiSampler.setMethod(getTestMethod());
        getRequestParameters().forEach((key, value) -> {
            apiSampler.addArgument(key, value);
        });
        apiSampler.setEnabled(true);
        apiSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        apiSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // Create Test plan
        HashTree testPlanHashTree = new ListedHashTree();
        HashTree threadGroupHashTree = testPlanHashTree.add(testPlan, threadGroup);

        // Add Config elements
        threadGroupHashTree.add(csvDataSet);
        threadGroupHashTree.add(cookieManager);
        threadGroupHashTree.add(defaultSampler);

        // Add HTTP samplers (and configurations, pre/post processors)
        HashTree loginSamplerHashTree = threadGroupHashTree.add(onceOnlyController, loginSampler);
        HashTree apiSamplerHashTree = threadGroupHashTree.add(apiSampler);

        switch (getTestMethod()) {
        case HttpDelete.METHOD_NAME:
            // fallthrough
        case HttpPut.METHOD_NAME:
            // fallthrough
        case HttpPost.METHOD_NAME:
            // Regex Extractor for CSRF token
            RegexExtractor regexExtractor = new RegexExtractor();
            regexExtractor.setName("Regular Expression Extractor");
            regexExtractor.setUseField("true"); // Find regex matches in response headers
            regexExtractor.setRefName("csrfToken");
            regexExtractor.setRegex("CSRF-TOKEN=(.+?);");
            regexExtractor.setTemplate("$1$");
            regexExtractor.setProperty(TestElement.TEST_CLASS, RegexExtractor.class.getName());
            regexExtractor.setProperty(TestElement.GUI_CLASS, RegexExtractorGui.class.getName());

            // HTTP Header Manager
            HeaderManager headerManager = new HeaderManager();
            headerManager.setName("HTTP Header Manager");
            headerManager.add(new Header("X-CSRF-TOKEN", "${csrfToken}"));
            headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
            headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());

            String requestBody = getRequestBody();
            if (requestBody != null && !requestBody.isEmpty()) {
                // Add Request Body
                apiSampler.addNonEncodedArgument("", requestBody, "");
                apiSampler.setPostBodyRaw(true);

                // Add corresponding Request Header
                headerManager.add(new Header("Content-Type", getRequestBodyContentType()));
            }

            // Add elements to test plan tree
            loginSamplerHashTree.add(regexExtractor);
            apiSamplerHashTree.add(headerManager);
            break;

        default:
            break;
        }

        return testPlanHashTree;
    }

}
