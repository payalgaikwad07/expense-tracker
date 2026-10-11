# Personal Finance Tracker (FinTrack)

A modern, responsive desktop Personal Finance Tracker built in **Java Swing** demonstrating core **Object-Oriented Programming & Design (OOPD)** principles.

---

## 🌟 Key Features

- **Transaction Management**:
  - Add, edit, and delete Income and Expense transactions.
  - Categorization with type-safe enums (`Category`) for Income (Salary, Investment, Business, etc.) and Expenses (Food, Rent, Travel, Utilities, etc.).
  - Search transactions in real time by description, category, or transaction type.

- **Monthly Dashboard**:
  - Quick month-by-month navigation (`<` and `>`).
  - Real-time metric cards showing **Total Income**, **Total Expenses**, **Net Balance**, and **Transaction Count**.

- **Budget Tracking & Alerts**:
  - Set monthly spending limits per expense category.
  - Visual progress bars showing budget consumption, warnings at 80% usage, and over-budget alerts.
  - Custom `BudgetExceededException` alerting users when adding expenses that exceed budget limits.

- **Data Visualizations**:
  - **Expense Breakdown Pie Chart**: Interactive donut/pie chart displaying monthly expense distribution by category with percentage values.
  - **Income vs. Expenses Bar Chart**: 6-month historical comparison chart rendered using native Java 2D Graphics (`Graphics2D`).

- **Data Persistence**:
  - Automatically loads and saves all transactions and budgets to CSV files (`data/transactions.csv` and `data/budgets.csv`).

---

## 🏗️ Project Architecture & OOP Principles

The project follows a clean, modular structure:

```
OOPD_FinanceTracker/
├── data/                             # CSV persistence files
│   ├── budgets.csv
│   └── transactions.csv
├── src/
│   └── com/
│       └── financetracker/
│           ├── Main.java             # Application entry point
│           ├── exception/            # Custom exception handling
│           │   └── BudgetExceededException.java
│           ├── model/                # Domain models & entities
│           │   ├── Budget.java
│           │   ├── Category.java     # Type-safe enum
│           │   ├── Expense.java      # Inherits from Transaction
│           │   ├── Income.java       # Inherits from Transaction
│           │   └── Transaction.java  # Abstract base class
│           ├── service/              # Business logic & services
│           │   ├── BudgetService.java
│           │   └── FinanceManager.java
│           ├── ui/                   # Swing GUI Presentation layer
│           │   ├── Dashboard.java    # Main JFrame window
│           │   ├── components/       # Custom UI panels & charts
│           │   │   ├── BarChartPanel.java
│           │   │   ├── BudgetPanel.java
│           │   │   ├── MetricCard.java
│           │   │   ├── PieChartPanel.java
│           │   │   └── TransactionPanel.java
│           │   └── dialogs/          # Modal forms
│           │       ├── AddTransactionDialog.java
│           │       ├── EditTransactionDialog.java
│           │       └── SetBudgetDialog.java
│           └── util/                 # Storage & helpers
│               └── FileStorage.java
├── desktop.ini                       # Windows shell display configuration
├── run.bat                           # Windows batch execution script
└── README.md                         # Documentation
```

### OOP Concepts Demonstrated:
1. **Abstraction**: `Transaction` abstract base class specifies common behavior and abstract methods (`getImpact()`, `getType()`).
2. **Inheritance & Polymorphism**: `Income` and `Expense` inherit from `Transaction` and override `getImpact()` (positive vs negative impact on balance).
3. **Encapsulation**: Private fields across all models with public getters and setters ensuring data integrity.
4. **Custom Exception Handling**: `BudgetExceededException` inherits from `Exception` to signal budget violations gracefully.
5. **Separation of Concerns**: Clean separation between presentation (`ui`), business logic (`service`), entities (`model`), and storage (`util`).

---

## 🚀 How to Run

### Requirements
- **Java Development Kit (JDK) 8+** (JDK 17 or 21 recommended).
- Java environment variables configured (`javac` and `java` available in PATH).

### Option 1: Quick Launch (Windows)
Double-click [`run.bat`](run.bat) or run it from the command line:
```cmd
run.bat
```

### Option 2: Manual Compilation & Execution
1. Compile the source code:
   ```cmd
   mkdir bin 2>nul
   javac -d bin src/com/financetracker/model/*.java src/com/financetracker/exception/*.java src/com/financetracker/service/*.java src/com/financetracker/util/*.java src/com/financetracker/*.java src/com/financetracker/ui/*.java src/com/financetracker/ui/components/*.java src/com/financetracker/ui/dialogs/*.java
   ```
2. Run the application:
   ```cmd
   java -cp bin com.financetracker.Main
   ```
