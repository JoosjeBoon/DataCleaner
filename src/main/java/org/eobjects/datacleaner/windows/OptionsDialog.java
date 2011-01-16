/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DatabaseDriversPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.HelpIcon;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.jdesktop.swingx.VerticalLayout;

public class OptionsDialog extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private final ImageManager imageManager = ImageManager.getInstance();
	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private final CloseableTabbedPane _tabbedPane;
	private final AnalyzerBeansConfiguration _configuration;
	private Timer _updateMemoryTimer;

	public OptionsDialog(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
		_tabbedPane = new CloseableTabbedPane();

		_tabbedPane.addTab("General", imageManager.getImageIcon("images/menu/options.png"), getGeneralTab());
		_tabbedPane.addTab("Database drivers", imageManager.getImageIcon("images/model/datastore.png"),
				new DatabaseDriversPanel(_configuration));
		_tabbedPane.addTab("Network", imageManager.getImageIcon("images/menu/network.png"), getNetworkTab());
		_tabbedPane.addTab("Performance", imageManager.getImageIcon("images/menu/performance.png"), getPerformanceTab());
		_tabbedPane.addTab("Memory", imageManager.getImageIcon("images/menu/memory.png"), getMemoryTab());

		_tabbedPane.setUnclosableTab(0);
		_tabbedPane.setUnclosableTab(1);
		_tabbedPane.setUnclosableTab(2);
		_tabbedPane.setUnclosableTab(3);
	}

	public void selectDatabaseDriversTab() {
		_tabbedPane.setSelectedIndex(1);
	}

	private DCPanel getGeneralTab() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		panel.setLayout(new VerticalLayout(4));

		final JCheckBox welcomeDialogShownOnStartupCheckBox = new JCheckBox("Display welcome dialog on start-up?");
		welcomeDialogShownOnStartupCheckBox.setSelected(userPreferences.isWelcomeDialogShownOnStartup());
		welcomeDialogShownOnStartupCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userPreferences.setWelcomeDialogShownOnStartup(welcomeDialogShownOnStartupCheckBox.isSelected());
			}
		});

		panel.add(welcomeDialogShownOnStartupCheckBox);

		return panel;
	}

	private DCPanel getNetworkTab() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		// TODO
		panel.add(new JLabel("TODO"));
		return panel;
	}

	private DCPanel getPerformanceTab() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);

		int row = 0;

		TaskRunner taskRunner = _configuration.getTaskRunner();
		WidgetUtils.addToGridBag(new JLabel("Task runner type:"), panel, 0, row);
		WidgetUtils.addToGridBag(new JLabel(taskRunner.getClass().getSimpleName()), panel, 1, row);
		WidgetUtils
				.addToGridBag(
						new HelpIcon(
								"The task runner is used to determine the execution strategy of Analysis jobs. The most common strategy for this is to use a multithreaded task runner which will spawn several threads to enable concurrent execution of jobs."),
						panel, 2, row);

		if (taskRunner instanceof MultiThreadedTaskRunner) {
			int numThreads = ((MultiThreadedTaskRunner) taskRunner).getNumThreads();

			if (numThreads > 0) {
				row++;
				WidgetUtils.addToGridBag(new JLabel("Thread pool size:"), panel, 0, row);
				WidgetUtils.addToGridBag(new JLabel("" + numThreads), panel, 1, row);
			}
		}

		row++;
		StorageProvider storageProvider = _configuration.getStorageProvider();
		WidgetUtils.addToGridBag(new JLabel("Storage provider type:"), panel, 0, row);
		WidgetUtils.addToGridBag(new JLabel(storageProvider.getClass().getSimpleName()), panel, 1, row);
		WidgetUtils
				.addToGridBag(
						new HelpIcon(
								"The storage provider is used for staging data during and after analysis, typically to store the results on disk in stead of holding everything in memory."),
						panel, 2, row);

		row++;
		DCLabel descriptionLabel = DCLabel
				.darkMultiLine("Performance options are currently not configurable while you're running the application. "
						+ "You need to edit the applications configuration file for this. The configuration file is named "
						+ "<b>conf.xml</b> and is located in the root of the folder where you've installed DataCleaner.");
		descriptionLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
		WidgetUtils.addToGridBag(descriptionLabel, panel, 0, row, 2, 1);
		return panel;
	}

	private DCPanel getMemoryTab() {
		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);

		final JLabel maxMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
		final JLabel totalMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
		final JLabel usedMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
		final JLabel freeMemoryLabel = new JLabel("? kb", JLabel.RIGHT);

		WidgetUtils.addToGridBag(new JLabel("Max available memory:"), panel, 0, 0);
		WidgetUtils.addToGridBag(maxMemoryLabel, panel, 1, 0);
		WidgetUtils.addToGridBag(new JLabel("Allocated memory:"), panel, 0, 1);
		WidgetUtils.addToGridBag(totalMemoryLabel, panel, 1, 1);
		WidgetUtils.addToGridBag(new JLabel("Used memory:"), panel, 0, 2);
		WidgetUtils.addToGridBag(usedMemoryLabel, panel, 1, 2);
		WidgetUtils.addToGridBag(new JLabel("Free memory:"), panel, 0, 3);
		WidgetUtils.addToGridBag(freeMemoryLabel, panel, 1, 3);

		_updateMemoryTimer = new Timer(1000, new ActionListener() {
			private final Runtime runtime = Runtime.getRuntime();
			private final NumberFormat nf = NumberFormat.getIntegerInstance();

			@Override
			public void actionPerformed(ActionEvent e) {

				long totalMemory = runtime.totalMemory();
				long freeMemory = runtime.freeMemory();
				long maxMemory = runtime.maxMemory();
				long usedMemory = totalMemory - freeMemory;

				if (maxMemory == Long.MAX_VALUE) {
					maxMemoryLabel.setText("(no limit)");
				} else {
					maxMemoryLabel.setText(nf.format(maxMemory / 1024) + " kb");
				}
				totalMemoryLabel.setText(nf.format(totalMemory / 1024) + " kb");
				usedMemoryLabel.setText(nf.format(usedMemory / 1024) + " kb");
				freeMemoryLabel.setText(nf.format(freeMemory / 1024) + " kb");
			}
		});
		_updateMemoryTimer.setInitialDelay(0);
		_updateMemoryTimer.start();

		JButton button = new JButton("Perform garbage collection");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.gc();
				System.runFinalization();
			}
		});
		WidgetUtils.addToGridBag(button, panel, 1, 4);

		return panel;
	}

	@Override
	protected boolean onWindowClosing() {
		boolean closing = super.onWindowClosing();
		if (closing) {
			if (_updateMemoryTimer != null) {
				_updateMemoryTimer.stop();
			}
		}
		return closing;
	}

	@Override
	protected JComponent getWindowContent() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(new DCBannerPanel("Options"), BorderLayout.NORTH);
		panel.add(_tabbedPane, BorderLayout.CENTER);

		final JButton closeButton = WidgetFactory.createButton("Close", "images/actions/save.png");
		closeButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialog.this.dispose();
			}
		});

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(closeButton);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		panel.add(toolBarPanel, BorderLayout.SOUTH);

		panel.setPreferredSize(500, 500);
		return panel;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return "Options";
	}

	@Override
	protected Image getWindowIcon() {
		return imageManager.getImage("images/menu/options.png");
	}
}
