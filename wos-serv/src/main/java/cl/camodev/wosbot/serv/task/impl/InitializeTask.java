package cl.camodev.wosbot.serv.task.impl;

import java.time.LocalDateTime;

import cl.camodev.wosbot.console.enumerable.EnumTemplates;
import cl.camodev.wosbot.console.enumerable.EnumTpMessageSeverity;
import cl.camodev.wosbot.console.enumerable.TpDailyTaskEnum;
import cl.camodev.wosbot.emulator.EmulatorManager;
import cl.camodev.wosbot.ex.StopExecutionException;
import cl.camodev.wosbot.ot.DTOImageSearchResult;
import cl.camodev.wosbot.ot.DTOProfiles;
import cl.camodev.wosbot.serv.impl.ServLogs;
import cl.camodev.wosbot.serv.task.DelayedTask;

public class InitializeTask extends DelayedTask {
	boolean isStarted = false;

	private final DTOProfiles profile;

	private final String EMULATOR_NUMBER;

	public InitializeTask(DTOProfiles profile, TpDailyTaskEnum tpTask) {
		super(tpTask, LocalDateTime.now());
		this.profile = profile;
		this.EMULATOR_NUMBER = profile.getEmulatorNumber().toString();
	}

	@Override
	protected void execute() {
		this.setRecurring(false);
		ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "Checking emulator status");
		while (!isStarted) {

			if (EmulatorManager.getInstance().getPlayerState(EMULATOR_NUMBER)) {
				isStarted = true;
				ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "emulator found");
			} else {
				ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "emulator not found, trying to start it");
				EmulatorManager.getInstance().launchPlayer(EMULATOR_NUMBER);
				ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "waiting 15 seconds before checking again");
				sleepTask(15000);
			}

		}

		if (!EmulatorManager.getInstance().isWhiteoutSurvivalInstalled(EMULATOR_NUMBER)) {
			ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "whiteout survival not installed, stopping queue");
			throw new StopExecutionException("Game not installed");
		} else {

//			EmulatorManager.getInstance().
			ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "launching game");
			EmulatorManager.getInstance().connectADB(profile.getEmulatorNumber().toString());
			EmulatorManager.getInstance().launchGame(EMULATOR_NUMBER);
			sleepTask(25000);

			boolean homeScreen = false;
			int attempts = 0;
			while (!homeScreen) {
				DTOImageSearchResult homeResult = EmulatorManager.getInstance().searchTemplate(EMULATOR_NUMBER, EnumTemplates.GAME_HOME_FURNACE.getTemplate(), 0, 0, 720, 1280, 90);
				if (homeResult.isFound()) {
					homeScreen = true;
					ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "home screen found");
				} else {
					ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "screen not found, waiting 5 seconds before checking again");
					EmulatorManager.getInstance().tapBackButton(EMULATOR_NUMBER);
					sleepTask(3000);
					attempts++;
				}

				if (attempts > 5) {
					ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "screen not found after 5 attempts, restarting emulator");
					EmulatorManager.getInstance().closePlayer(EMULATOR_NUMBER);
					EmulatorManager.getInstance().restartAdbServer();
					isStarted = false;
					this.setRecurring(true);
					break;
				}
			}

		}
	}

	public String getEmulatorNumber() {
		return EMULATOR_NUMBER;
	}

}