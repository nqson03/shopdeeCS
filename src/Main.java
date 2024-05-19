import MainSystem.SystemManager;
import Utils.Utils;

public class Main {
    public static void main(String[] args) {
        SystemManager system = SystemManager.getInstance();
        while (true) {
            try {
                system.displayMenu();
            } catch (Exception e) {
                System.out.println(e.getMessage());
//                e.printStackTrace();
                if (!Utils.promptInput("Restart program? (y/n): ").equalsIgnoreCase("y")) {
                    break;
                }
            }
        }
    }
}
