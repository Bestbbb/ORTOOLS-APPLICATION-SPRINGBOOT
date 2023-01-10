package priorityTest;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

public class EarlinessTardinessCostSampleSat {
    public static void main(String[] args) throws Exception {
        Loader.loadNativeLibraries();
        long earlinessDate = 5;
        long earlinessCost = 8;
        long latenessDate = 15;
        long latenessCost = 12;

        // Create the CP-SAT model.
        CpModel model = new CpModel();

        // Declare our primary variable.
        IntVar x = model.newIntVar(0, 20, "x");

        // Create the expression variable and implement the piecewise linear function.
        //
        //  \        /
        //   \______/
        //   ed    ld
        //
        long largeConstant = 1000;
        IntVar expr = model.newIntVar(0, largeConstant, "expr");

        // Link together expr and the 3 segment.
        // First segment: y == earlinessCost * (earlinessDate - x).
        // Second segment: y = 0
        // Third segment: y == latenessCost * (x - latenessDate).
        model.addMaxEquality(expr,
                new LinearExpr[] {LinearExpr.newBuilder()
                        .addTerm(x, -earlinessCost)
                        .add(earlinessCost * earlinessDate)
                        .build(),
                        LinearExpr.constant(0),
                        LinearExpr.newBuilder()
                                .addTerm(x, latenessCost)
                                .add(-latenessCost * latenessDate)
                                .build()});

        // Search for x values in increasing order.
        model.addDecisionStrategy(new IntVar[] {x},
                DecisionStrategyProto.VariableSelectionStrategy.CHOOSE_FIRST,
                DecisionStrategyProto.DomainReductionStrategy.SELECT_MIN_VALUE);

        // Create the solver.
        CpSolver solver = new CpSolver();

        // Force the solver to follow the decision strategy exactly.
        solver.getParameters().setSearchBranching(SatParameters.SearchBranching.FIXED_SEARCH);
        // Tell the solver to enumerate all solutions.
        solver.getParameters().setEnumerateAllSolutions(true);

        // Solve the problem with the printer callback.
        solver.solve(model, new CpSolverSolutionCallback() {
            public CpSolverSolutionCallback init(IntVar[] variables) {
                variableArray = variables;
                return this;
            }

            @Override
            public void onSolutionCallback() {
                for (IntVar v : variableArray) {
                    System.out.printf("%s=%d ", v.getName(), value(v));
                }
                System.out.println();
            }

            private IntVar[] variableArray;
        }.init(new IntVar[] {x, expr}));

    }
}
