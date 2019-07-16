package net.surfm.ethos;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	@Value("${fixedRate}")
	private int fixedRate = 0;
	@Value("${initialDelay}")
	private int initialDelay = 0;
	@Value("${filePath}")
	private String filePath;
	@Value("${minFileCount}")
	private long minFileCount;

	private boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

	private long lastCount;

	@Scheduled(fixedRateString = "${fixedRate}", initialDelayString = "${initialDelay}")
	public void reportCurrentTime() throws IOException, InterruptedException {

		if (StringUtils.isBlank(filePath))
			throw new NullPointerException("not get file path");

		long numOfLines = getFileLine(filePath);
		log.info(" numOfLines {}", numOfLines);
		if (lastCount <= minFileCount) {
			lastCount = numOfLines;
			log.info(" init Line {} current line {}", minFileCount, numOfLines);
		} else if (lastCount == numOfLines) {
			callRestart();
		}

		log.info(fixedRate + " The time is now {}", dateFormat.format(new Date()));
	}

	private long getFileLine(String fileN) throws FileNotFoundException, IOException {
		try (FileReader input = new FileReader(fileN); LineNumberReader count = new LineNumberReader(input);) {
			while (count.skip(Long.MAX_VALUE) > 0) {
				// Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read
				// the entire file
			}

			return count.getLineNumber() + 1; // +1 because line index starts at 0
		}
	}

	private void callRestart() throws IOException, InterruptedException {

		String homeDirectory = System.getProperty("user.home");
		Process process;
		if (isWindows) {
			process = Runtime.getRuntime().exec(String.format("cmd.exe /c dir %s", homeDirectory));
		} else {
			process = Runtime.getRuntime().exec("sudo r");
		}
		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
		Executors.newSingleThreadExecutor().submit(streamGobbler);
		int exitCode = process.waitFor();
		log.info(" callRestart {}", exitCode);
		System.exit(0);
	}
}