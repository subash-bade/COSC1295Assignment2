package au.edu.rmit.cosc1295.carehome.app;

import au.edu.rmit.cosc1295.carehome.ui.CliApp;

public final class Main {
    public static void main(String[] args) {
        var ctx = AppContext.loadOrInit();
        new CliApp().run();
        try {
            StateSerializer.save(ctx.state, ctx.saveFile);
            System.out.println("State saved to " + ctx.saveFile);
        } catch (Exception e) {
            System.err.println("Failed to save: " + e.getMessage());
        }
    }
}
