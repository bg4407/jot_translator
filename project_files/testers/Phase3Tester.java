package testers;

/**
 * Phase 3 tester for Jott semantic analysis.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borneil Gope, Jatin Jain
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import provided.*;

public class Phase3Tester {
    private static class TestCase {
        String testName;
        String fileName;
        boolean expectError;

        public TestCase(String testName, String fileName, boolean expectError) {
            this.testName = testName;
            this.fileName = fileName;
            this.expectError = expectError;
        }
    }

    private ArrayList<TestCase> testCases;

    private void createTestCases() {
        testCases = new ArrayList<>();
        testCases.add(new TestCase("hello world", "helloWorld.jott", false));
        testCases.add(new TestCase("provided example1", "providedExample1.jott", false));
        testCases.add(new TestCase("valid loop", "validLoop.jott", false));
        testCases.add(new TestCase("if stmt returns", "ifStmtReturns.jott", false));
        testCases.add(new TestCase("func return in expr", "funcReturnInExpr.jott", false));
        testCases.add(new TestCase("larger valid", "largerValid.jott", false));

        testCases.add(new TestCase("function call param invalid", "funcCallParamInvalid.jott", true));
        testCases.add(new TestCase("function not defined", "funcNotDefined.jott", true));
        testCases.add(new TestCase("wrong function param type", "funcWrongParamType.jott", true));
        testCases.add(new TestCase("main return not void", "mainReturnNotInt.jott", true));
        testCases.add(new TestCase("mismatched return type", "mismatchedReturn.jott", true));
        testCases.add(new TestCase("missing function params", "missingFuncParams.jott", true));
        testCases.add(new TestCase("missing main", "missingMain.jott", true));
        testCases.add(new TestCase("missing return", "missingReturn.jott", true));
        testCases.add(new TestCase("no return if", "noReturnIf.jott", true));
        testCases.add(new TestCase("no return while", "noReturnWhile.jott", true));
        testCases.add(new TestCase("return id mismatch", "returnId.jott", true));
        testCases.add(new TestCase("void return has value", "voidReturn.jott", true));
        testCases.add(new TestCase("while keyword id error", "whileKeyword.jott", true));
    }

    private boolean runTest(TestCase test) {
        System.out.println("Running Test: " + test.testName);
        ArrayList<Token> tokens = JottTokenizer.tokenize("phase3testcases/" + test.fileName);
        if (tokens == null) {
            if (test.expectError) {
                return true;
            }
            System.err.println("\tFailed Test: " + test.testName);
            System.err.println("\t\tTokenizer returned null");
            return false;
        }

        JottTree root = JottParser.parse(tokens);
        if (root == null) {
            if (test.expectError) {
                return true;
            }
            System.err.println("\tFailed Test: " + test.testName);
            System.err.println("\t\tExpected a JottTree and got null");
            return false;
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        if (!analyzer.validateProgram((ProgramStructureNode.ProgramNode) root)) {
            if (test.expectError) {
                return true;
            }
            System.err.println("\tFailed Test: " + test.testName);
            System.err.println("\t\tExpected valid phase3 program but got semantic errors");
            return false;
        }

        if (test.expectError) {
            System.err.println("\tFailed Test: " + test.testName);
            System.err.println("\t\tExpected semantic error but parser returned a tree");
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        Phase3Tester tester = new Phase3Tester();
        tester.createTestCases();
        int passed = 0;
        int total = tester.testCases.size();

        for (TestCase test : tester.testCases) {
            if (tester.runTest(test)) {
                passed++;
                System.out.println("\tPassed\n");
            } else {
                System.out.println("\tFailed\n");
            }
        }

        System.out.printf("Passed: %d/%d%n", passed, total);
    }
}
