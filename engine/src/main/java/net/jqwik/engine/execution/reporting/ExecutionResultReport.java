package net.jqwik.engine.execution.reporting;

import java.lang.reflect.*;
import java.util.*;

import org.opentest4j.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.descriptor.*;
import net.jqwik.engine.execution.lifecycle.*;
import net.jqwik.engine.support.*;

public class ExecutionResultReport {

	private static final String TRIES_KEY = "tries";
	private static final String CHECKS_KEY = "checks";
	private static final String GENERATION_KEY = "generation";
	private static final String EDGE_CASES_MODE_KEY = "edge-cases#mode";
	private static final String EDGE_CASES_TOTAL_KEY = "edge-cases#total";
	private static final String EDGE_CASES_TRIED_KEY = "edge-cases#tried";
	private static final String AFTER_FAILURE_KEY = "after-failure";
	private static final String SEED_KEY = "seed";
	private static final String SAMPLE_HEADLINE = "Sample";
	private static final String SHRUNK_SAMPLE_HEADLINE = "Shrunk Sample";
	private static final String ORIGINAL_SAMPLE_HEADLINE = "Original Sample";

	public static String from(
		PropertyMethodDescriptor methodDescriptor,
		ExtendedPropertyExecutionResult executionResult
	) {
		return buildJqwikReport(
			methodDescriptor.getConfiguration().getAfterFailureMode(),
			methodDescriptor.getTargetMethod(),
			executionResult
		);
	}

	private static String buildJqwikReport(
		AfterFailureMode afterFailureMode,
		Method propertyMethod,
		ExtendedPropertyExecutionResult executionResult
	) {
		StringBuilder reportLines = new StringBuilder();

		appendThrowableMessage(reportLines, executionResult);
		appendFixedSizedProperties(reportLines, executionResult, afterFailureMode);
		appendSamples(reportLines, propertyMethod, executionResult);

		return reportLines.toString();
	}

	private static void appendSamples(
		StringBuilder reportLines,
		final Method propertyMethod,
		ExtendedPropertyExecutionResult executionResult
	) {
		executionResult.shrunkSample().ifPresent(shrunkSample -> {
			if (!shrunkSample.isEmpty()) {
				String shrunkSampleHeadline = String.format("%s (%s steps)", SHRUNK_SAMPLE_HEADLINE, executionResult.countShrinkingSteps());
				SampleReporter.reportSample(reportLines, propertyMethod, shrunkSample, shrunkSampleHeadline);
			}
		});

		executionResult.originalSample().ifPresent(originalSample -> {
			String originalSampleHeadline = executionResult.shrunkSample().isPresent() ? ORIGINAL_SAMPLE_HEADLINE : SAMPLE_HEADLINE;
			if (!originalSample.isEmpty()) {
				SampleReporter.reportSample(reportLines, propertyMethod, originalSample, originalSampleHeadline);
			}
		});
	}

	private static void appendFixedSizedProperties(
		StringBuilder reportLines,
		ExtendedPropertyExecutionResult executionResult,
		AfterFailureMode afterFailureMode
	) {
		List<String> propertiesLines = new ArrayList<>();
		int countTries = 0;
		int countChecks = 0;
		String generationMode = "<none>";
		String edgeCasesMode = "<none>";
		String randomSeed = "<none>";
		String helpGenerationMode = "";
		String helpEdgeCasesMode = "";

		if (executionResult.isExtended()) {
			countTries = executionResult.countTries();
			countChecks = executionResult.countChecks();
			generationMode = executionResult.generation().name();
			edgeCasesMode = executionResult.edgeCases().mode().name();
			randomSeed = executionResult.randomSeed();
			helpGenerationMode = helpGenerationMode(executionResult.generation());
			helpEdgeCasesMode = helpEdgeCasesMode(executionResult.edgeCases().mode());
		}

		appendProperty(propertiesLines, TRIES_KEY, Integer.toString(countTries), "# of calls to property");
		appendProperty(propertiesLines, CHECKS_KEY, Integer.toString(countChecks), "# of not rejected calls");
		appendProperty(propertiesLines, GENERATION_KEY, generationMode, helpGenerationMode);
		if (afterFailureMode != AfterFailureMode.NOT_SET) {
			appendProperty(propertiesLines, AFTER_FAILURE_KEY, afterFailureMode.name(), helpAfterFailureMode(afterFailureMode));
		}
		appendProperty(propertiesLines, EDGE_CASES_MODE_KEY, edgeCasesMode, helpEdgeCasesMode);
		if (executionResult.edgeCases().mode().activated()) {
			appendProperty(propertiesLines, EDGE_CASES_TOTAL_KEY, executionResult.edgeCases().total(), "# of all combined edge cases");
			appendProperty(propertiesLines, EDGE_CASES_TRIED_KEY, executionResult.edgeCases()
																				 .tried(), "# of edge cases tried in current run");
		}
		appendProperty(propertiesLines, SEED_KEY, randomSeed, "random seed to reproduce generated values");

		int halfBorderLength =
			(propertiesLines.stream().mapToInt(String::length).max().orElse(50) - 37) / 2 + 1;
		String halfBorder = String.join("", Collections.nCopies(halfBorderLength, "-"));

		reportLines.append(String.format("%n"));
		reportLines.append(buildLine("", "|" + halfBorder + "jqwik" + halfBorder));
		propertiesLines.forEach(reportLines::append);

	}

	private static void appendThrowableMessage(StringBuilder reportLines, ExtendedPropertyExecutionResult executionResult) {
		if (executionResult.status() != PropertyExecutionResult.Status.SUCCESSFUL) {
			Throwable throwable = executionResult.throwable().orElse(new AssertionFailedError(null));
			String assertionClass = throwable.getClass().getName();
			reportLines.append(String.format("%n  %s", assertionClass));
			List<String> assertionMessageLines = JqwikStringSupport.toLines(throwable.getMessage());
			if (!assertionMessageLines.isEmpty()) {
				reportLines.append(":");
				for (String line : assertionMessageLines) {
					reportLines.append(String.format("%n    %s", line));
				}
			}
			reportLines.append(String.format("%n"));
		}
	}

	private static String helpAfterFailureMode(AfterFailureMode afterFailureMode) {
		switch (afterFailureMode) {
			case RANDOM_SEED:
				return "use a new random seed";
			case PREVIOUS_SEED:
				return "use the previous seed";
			case SAMPLE_ONLY:
				return "only try the previously failed sample";
			case SAMPLE_FIRST:
				return "try previously failed sample, then previous seed";
			default:
				return "RANDOM_SEED, PREVIOUS_SEED or SAMPLE_FIRST";
		}
	}

	private static String helpGenerationMode(GenerationMode generation) {
		switch (generation) {
			case RANDOMIZED:
				return "parameters are randomly generated";
			case EXHAUSTIVE:
				return "parameters are exhaustively generated";
			case DATA_DRIVEN:
				return "parameters are taken from data provider";
			default:
				return "RANDOMIZED, EXHAUSTIVE or DATA_DRIVEN";
		}
	}

	private static String helpEdgeCasesMode(EdgeCasesMode edgeCases) {
		switch (edgeCases) {
			case FIRST:
				return "edge cases are generated first";
			case MIXIN:
				return "edge cases are mixed in";
			case NONE:
				return "edge cases are not explicitly generated";
			default:
				return "FIRST, MIXIN or NONE";
		}
	}

	private static void appendProperty(List<String> propertiesLines, String triesKey, Object value, String comment) {
		propertiesLines.add(buildPropertyLine(triesKey, value.toString(), comment));
	}

	private static String buildPropertyLine(String key, String value, String help) {
		return buildLine(buildProperty(key, value), String.format("| %s", help));
	}

	private static String buildProperty(String key, String value) {
		return String.format("%s = %s", key, value);
	}

	private static String buildLine(String body, String helpString) {
		return String.format("%-30s%s%n", body, helpString);
	}

}
