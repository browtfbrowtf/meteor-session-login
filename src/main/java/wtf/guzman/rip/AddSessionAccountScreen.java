package wtf.guzman.rip;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.accounts.AccountsScreen;
import meteordevelopment.meteorclient.gui.screens.accounts.AddAccountScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.mixin.FileCacheAccessor;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.mixin.PlayerSkinProviderAccessor;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AddSessionAccountScreen extends AddAccountScreen {
    public AddSessionAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Log in with a session token", parent);
    }


    public static void setSession(Session session) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor) MeteorClient.mc;
        mca.setSession(session);
        UserApiService apiService = mca.getAuthenticationService().createUserApiService(session.getAccessToken());
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManager(new SocialInteractionsManager(MeteorClient.mc, apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, session, MeteorClient.mc.runDirectory.toPath()));
        mca.setAbuseReportContext(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
        mca.setGameProfileFuture(CompletableFuture.supplyAsync(() -> MeteorClient.mc.getSessionService().fetchProfile(MeteorClient.mc.getSession().getUuidOrNull(), true), Util.getIoWorkerExecutor()));
    }

    public boolean login() {
        YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(((MinecraftClientAccessor)MeteorClient.mc).getProxy());
        applyLoginEnvironment(authenticationService, authenticationService.createMinecraftSessionService());
        return true;
    }

    public static void applyLoginEnvironment(YggdrasilAuthenticationService authService, MinecraftSessionService sessService) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor)MeteorClient.mc;
        mca.setAuthenticationService(authService);
        SignatureVerifier.create(authService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
        mca.setSessionService(sessService);
        var skinCache = ((PlayerSkinProviderAccessor)MeteorClient.mc.getSkinProvider()).getSkinCache();
        Path skinCachePath = ((FileCacheAccessor)skinCache).getDirectory();
        mca.setSkinProvider(new PlayerSkinProvider(skinCachePath, sessService, MeteorClient.mc));
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Token
        t.add(theme.label("Token: "));

        WTextBox token = t.add(theme.textBox("")).minWidth(400).expandX().widget();
        token.setFocused(true);
        t.row();

        // Add
        add = t.add(theme.button("Log in")).expandX().widget();
        add.action = () -> {
            if (!token.get().isEmpty()) {
                String session = token.get();
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile/").openConnection();
                    c.setRequestProperty("Content-type", "application/json");
                    c.setRequestProperty("Authorization", "Bearer " + session);
                    c.setDoOutput(true);
                    JsonObject json = new JsonParser().parse(IOUtils.toString(c.getInputStream())).getAsJsonObject();
                    String username = json.get("name").getAsString();
                    String uuid = json.get("id").getAsString();

                    StringBuilder builder = new StringBuilder(uuid);
                    builder.insert(8, "-");
                    builder.insert(13, "-");
                    builder.insert(18, "-");
                    builder.insert(23, "-");
                    uuid = builder.toString();

                    System.out.println(uuid);

                    setSession(new Session(username, UUID.fromString(uuid), session, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
                    if (!login()) {
                        System.out.println("Failed to complete login");
                    };
                    this.close();
                    parent.locked = false;
                    parent.close();
                }catch(Exception ignored){
                    ignored.printStackTrace();
                }
                //AccountsScreen.addAccount(this, parent, new SessionAccount(session));
            }
        };

        enterAction = add.action;
    }
}
