package cl.camodev.wosbot.serv.task.impl;

import java.time.LocalDateTime;

import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import cl.camodev.wosbot.console.enumerable.EnumTemplates;
import cl.camodev.wosbot.console.enumerable.EnumTpMessageSeverity;
import cl.camodev.wosbot.console.enumerable.TpDailyTaskEnum;
import cl.camodev.wosbot.emulator.EmulatorManager;
import cl.camodev.wosbot.ot.DTOImageSearchResult;
import cl.camodev.wosbot.ot.DTOPoint;
import cl.camodev.wosbot.ot.DTOProfiles;
import cl.camodev.wosbot.serv.impl.ServLogs;
import cl.camodev.wosbot.serv.task.DelayedTask;

public class ExplorationTask extends DelayedTask {

	private final DTOProfiles profile;

	private final String EMULATOR_NUMBER;

	public ExplorationTask(DTOProfiles list, TpDailyTaskEnum tpTask) {
		super(tpTask, LocalDateTime.now());
		this.profile = list;
		this.EMULATOR_NUMBER = list.getEmulatorNumber().toString();
	}

	@Override
	protected void execute() {
		ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "Starting Task");

		// Buscar la plantilla de la pantalla HOME
		DTOImageSearchResult homeResult = EmulatorManager.getInstance().searchTemplate(EMULATOR_NUMBER, EnumTemplates.GAME_HOME_FURNACE.getTemplate(), 0, 0, 720, 1280, 90);
		DTOImageSearchResult worldResult = EmulatorManager.getInstance().searchTemplate(EMULATOR_NUMBER, EnumTemplates.GAME_HOME_WORLD.getTemplate(), 0, 0, 720, 1280, 90);
		if (homeResult.isFound() || worldResult.isFound()) {
			ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "going to exploration");
			EmulatorManager.getInstance().tapAtRandomPoint(EMULATOR_NUMBER, new DTOPoint(40, 1190), new DTOPoint(100, 1250));
			sleepTask(3000);
			DTOImageSearchResult claimResult = EmulatorManager.getInstance().searchTemplate(EMULATOR_NUMBER, EnumTemplates.EXPLORATION_CLAIM.getTemplate(), 0, 0, 720, 1280, 95);
			if (claimResult.isFound()) {
				ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "claiming rewards");
				EmulatorManager.getInstance().tapAtRandomPoint(EMULATOR_NUMBER, new DTOPoint(560, 900), new DTOPoint(670, 940));
				sleepTask(3000);
				EmulatorManager.getInstance().tapAtRandomPoint(EMULATOR_NUMBER, new DTOPoint(230, 890), new DTOPoint(490, 960));
				sleepTask(3000);

				EmulatorManager.getInstance().tapAtRandomPoint(EMULATOR_NUMBER, new DTOPoint(230, 890), new DTOPoint(490, 960));
				sleepTask(200);
				EmulatorManager.getInstance().tapAtRandomPoint(EMULATOR_NUMBER, new DTOPoint(230, 890), new DTOPoint(490, 960));
				sleepTask(200);
				EmulatorManager.getInstance().tapAtRandomPoint(EMULATOR_NUMBER, new DTOPoint(230, 890), new DTOPoint(490, 960));
				sleepTask(200);

				int offset = profile.getConfig(EnumConfigurationKey.INT_EXPLORATION_CHEST_OFFSET, Integer.class);
				this.reschedule(LocalDateTime.now().plusHours(offset));
				ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "rescheduled for " + offset + " hours");

			} else {
				int offset = profile.getConfig(EnumConfigurationKey.INT_EXPLORATION_CHEST_OFFSET, Integer.class);
				this.reschedule(LocalDateTime.now().plusHours(offset));
				ServLogs.getServices().appendLog(EnumTpMessageSeverity.INFO, taskName, profile.getName(), "no rewards to claim, rescheduled for " + offset + " hours");
			}
			EmulatorManager.getInstance().tapBackButton(EMULATOR_NUMBER);

		} else {
			ServLogs.getServices().appendLog(EnumTpMessageSeverity.WARNING, taskName, profile.getName(), "Home not found");
			EmulatorManager.getInstance().tapBackButton(EMULATOR_NUMBER);

		}
		sleepTask(3000);

	}

}
