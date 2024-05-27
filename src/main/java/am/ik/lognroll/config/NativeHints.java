package am.ik.lognroll.config;

import java.util.HashSet;

import org.eclipse.jetty.http.pathmap.PathSpecSet;
import org.eclipse.jetty.util.AsciiLowerCaseSet;
import org.eclipse.jetty.util.ClassMatcher;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(NativeHints.RuntimeHints.class)
public class NativeHints {

	public static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {
			try {
				hints.reflection()
					.registerConstructor(ClassMatcher.ByPackage.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByClass.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByPackageOrName.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByLocation.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByModule.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByLocationOrModule.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(PathSpecSet.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(AsciiLowerCaseSet.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(HashSet.class.getConstructor(), ExecutableMode.INVOKE);
			}
			catch (NoSuchMethodException e) {
				throw new IllegalStateException(e);
			}
		}

	}

}