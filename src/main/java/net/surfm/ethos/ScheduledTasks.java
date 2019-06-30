package net.surfm.ethos;

import java.io.IOException;
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

		try (Stream<String> lines = Files.lines(Paths.get(filePath), Charset.defaultCharset())) {

			long numOfLines = lines.count();
			log.info(" numOfLines {}", numOfLines);
			if (lastCount <= minFileCount) {
				lastCount = numOfLines;
				log.info(" init Line {} current line {}",minFileCount, numOfLines);
			} else if (lastCount == numOfLines) {
				callRestart();
			}
		}

		log.info(fixedRate + " The time is now {}", dateFormat.format(new Date()));
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