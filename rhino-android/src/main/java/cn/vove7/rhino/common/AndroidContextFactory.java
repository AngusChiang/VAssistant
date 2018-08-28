package cn.vove7.rhino.common;



import android.os.Build;
import android.support.annotation.VisibleForTesting;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.tools.shell.ShellContextFactory;

import java.io.File;

/**
 * Ensures that the classLoader used is correct
 *
 * @author F43nd1r
 * @since 11.01.2016
 */

public class AndroidContextFactory extends ShellContextFactory {
    private final File cacheDirectory;

    /**
     * Create a new factory. It will cache generated code in the given directory
     *
     * @param cacheDirectory the cache directory
     */
    public AndroidContextFactory(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        initApplicationClassLoader(createClassLoader(AndroidContextFactory.class.getClassLoader()));
    }

    /**
     * Create a ClassLoader which is able to deal with bytecode
     *
     * @param parent the parent of the create classloader
     * @return a new ClassLoader
     */
    /**
     * Create a ClassLoader which is able to deal with bytecode
     *
     * @param parent the parent of the create classloader
     * @return a new ClassLoader
     */
    @VisibleForTesting
    @Override
    public BaseAndroidClassLoader createClassLoader(ClassLoader parent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new InMemoryAndroidClassLoader(parent);
        }
        return new FileAndroidClassLoader(parent, cacheDirectory);
    }


    @Override
    protected void observeInstructionCount(Context cx, int instructionCount) {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Interrupted");
        }
    }

    @Override
    protected Context makeContext() {
        Context cx = super.makeContext();
        cx.setInstructionObserverThreshold(10000);
        return cx;
    }


}
