package space.itoncek.trailcompass.modules;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.pkg.objects.User;

public class GameManagerModule {

    private final TrailServer server;

    public GameManagerModule(TrailServer server) {
        this.server = server;
    }

    public void getCurrentHider(@NotNull Context ctx) {
        if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

        int currentHiderId = server.db.getCurrentHiderId();

        ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result(currentHiderId+"");
    }

    public void setCurrentHider(@NotNull Context ctx) {
        if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

        User u = server.login.getUser(ctx);

        if(u == null || !u.admin()) {
            ctx.status(HttpStatus.UNAUTHORIZED).contentType(ContentType.TEXT_PLAIN).result("401 Unauthorized");
            return;
        }

        if (server.db.setCurrentHider(Integer.parseInt(ctx.body()))) {
            ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result("200 OK");
        } else {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
        }
    }

    public void getGameState(@NotNull Context ctx) {

    }

    public void start(@NotNull Context ctx) {

    }
}
