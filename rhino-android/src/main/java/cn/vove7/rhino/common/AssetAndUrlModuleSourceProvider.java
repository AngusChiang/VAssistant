package cn.vove7.rhino.common;

import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * ...
 * Created by Stardust on 2017/5/9.
 * 重定向require资源
 */

public class AssetAndUrlModuleSourceProvider extends UrlModuleSourceProvider {

    private static final String MODULES_PATH = "rhino_require";
    private android.content.Context mContext;
    private List<String> mModules;
    private final URI mBaseURI = URI.create("file:///android_asset/" + MODULES_PATH);

    public AssetAndUrlModuleSourceProvider(android.content.Context context, List<URI> list) {
        super(list, null);
        mContext = context;
        try {
            mModules = Arrays.asList(mContext.getAssets().list(MODULES_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ModuleSource loadFromPrivilegedLocations(String moduleId, Object validator) throws IOException, URISyntaxException {
        String moduleIdWithExtension = moduleId;
        if (!moduleIdWithExtension.endsWith(".js")) {
            moduleIdWithExtension += ".js";
        }
        if (mModules.contains(moduleIdWithExtension)) {
            return new ModuleSource(new InputStreamReader(mContext.getAssets()
                    .open(MODULES_PATH + "/" + moduleIdWithExtension)), null,
                    URI.create(moduleIdWithExtension), mBaseURI, validator);
        }
        return super.loadFromPrivilegedLocations(moduleId, validator);
    }
}