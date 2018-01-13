package net.jqwik.api.providers;

import java.math.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.providers.*;

class DefaultArbitraryProvidersTests {

	private enum AnEnum {
		One,
		Two,
		Three
	}

	private static class Person {

	}

	@Property
	boolean intParam(@ForAll int aValue) {
		return true;
	}

	@Property
	boolean integerParam(@ForAll Integer aValue) {
		return aValue != null;
	}

	@Property
	boolean longParam(@ForAll long aValue) {
		return true;
	}

	@Property
	boolean longerParam(@ForAll Long aValue) {
		return aValue != null;
	}

	@Property
	boolean doubleParam(@ForAll double aValue) {
		return true;
	}

	@Property
	boolean doublerParam(@ForAll Double aValue) {
		return aValue != null;
	}

	@Property
	boolean floatParam(@ForAll float aValue) {
		return true;
	}

	@Property
	boolean floaterParam(@ForAll Float aValue) {
		return aValue != null;
	}

	@Property
	boolean booleanParam(@ForAll boolean aValue) {
		return true;
	}

	@Property
	boolean boxedBooleanParam(@ForAll Boolean aValue) {
		return aValue != null;
	}

	@Property
	boolean charParam(@ForAll char aValue) {
		return true;
	}

	@Property
	boolean boxedCharacterParam(@ForAll Character aValue) {
		return aValue != null;
	}

	@Property
	boolean enumParam(@ForAll AnEnum aValue) {
		return true;
	}

	@Property
	boolean bigIntegerParam(@ForAll BigInteger aValue) {
		return aValue != null;
	}

	@Property
	boolean bigDecimalParam(@ForAll BigDecimal aValue) {
		return aValue != null;
	}

	@Property
	boolean stringParam(@ForAll String aValue) {
		return aValue != null;
	}

	@Property
	boolean integerList(@ForAll List<Integer> aValue) {
		return aValue != null;
	}

	@Property
	boolean integerArray(@ForAll Integer[] aValue) {
		return aValue != null;
	}

	@Property
	boolean integerSet(@ForAll Set<Integer> aValue) {
		return aValue != null;
	}

	@Property
	boolean integerStream(@ForAll Stream<Integer> aValue) {
		return aValue != null;
	}

	@Property
	boolean integerOptional(@ForAll Optional<Integer> aValue) {
		return aValue != null;
	}

	@Group
	class Registration implements AutoCloseable {

		final ArbitraryProvider personProvider;

		Registration() {
			personProvider = new ArbitraryProvider() {
				@Override
				public boolean canProvideFor(GenericType targetType) {
					return targetType.isAssignableFrom(Person.class);
				}

				@Override
				public Arbitrary<?> provideFor(GenericType targetType, Function<GenericType, Optional<Arbitrary<?>>> subtypeProvider) {
					return Arbitraries.of(new Person());
				}
			};
			DefaultArbitraryProviders.register(personProvider);
		}

		@Property
		boolean personCanBeGenerated(@ForAll Person aPerson) {
			return aPerson != null;
		}

		@Example
		boolean manuallyRegisteredProviderIsPartOfDefaultProviders() {
			return DefaultArbitraryProviders.getProviders().contains(personProvider);
		}

		@Example
		boolean manuallyRegisteredProviderCanBeUnregistered() {
			DefaultArbitraryProviders.unregister(personProvider);
			return !DefaultArbitraryProviders.getProviders().contains(personProvider);
		}

		@Example
		boolean manuallyRegisteredProviderCanBeUnregisteredByClass() {
			DefaultArbitraryProviders.unregister(personProvider.getClass());
			return !DefaultArbitraryProviders.getProviders().contains(personProvider);
		}

		@Override
		public void close() throws Exception {
			DefaultArbitraryProviders.unregister(personProvider);
		}
	}
}