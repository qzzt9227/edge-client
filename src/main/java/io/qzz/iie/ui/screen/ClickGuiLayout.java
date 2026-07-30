package io.qzz.iie.ui.screen;

import io.qzz.iie.ui.layout.Rect;

record ClickGuiLayout(
	int windowX,
	int windowY,
	int windowWidth,
	int windowHeight,
	int sidebarX,
	int sidebarY,
	int sidebarWidth,
	int sidebarHeight,
	int contentX,
	int contentY,
	int contentWidth,
	int contentHeight,
	int padding,
	int smallGap,
	int radius,
	int innerRadius,
	int headerHeight,
	int navigationRowHeight,
	int moduleRowHeight,
	int settingRowHeight,
	int scrollbarWidth
) {
	static ClickGuiLayout calculate(
		int screenWidth,
		int screenHeight,
		boolean sidebarCollapsed
	) {
		int margin = Math.clamp(Math.min(screenWidth, screenHeight) / 32, 8, 18);
		int windowWidth = Math.max(
			1,
			Math.min(screenWidth - margin * 2, Math.min(1120, (int) Math.round(screenWidth * 0.86)))
		);
		int windowHeight = Math.max(
			1,
			Math.min(screenHeight - margin * 2, Math.min(720, (int) Math.round(screenHeight * 0.84)))
		);
		int windowX = (screenWidth - windowWidth) / 2;
		int windowY = (screenHeight - windowHeight) / 2;
		double scale = Math.clamp(
			Math.min(windowWidth / 900.0, windowHeight / 560.0),
			0.65,
			1.0
		);
		int border = Math.max(3, (int) Math.round(5 * scale));
		int gap = Math.max(3, (int) Math.round(5 * scale));
		int padding = Math.max(10, (int) Math.round(18 * scale));
		int smallGap = Math.max(3, (int) Math.round(6 * scale));
		int headerHeight = Math.max(44, (int) Math.round(62 * scale));
		int navigationRowHeight = Math.max(36, (int) Math.round(46 * scale));
		int moduleRowHeight = Math.max(46, (int) Math.round(58 * scale));
		int settingRowHeight = Math.max(52, (int) Math.round(66 * scale));
		int sidebarWidth = sidebarCollapsed
			? 0
			: Math.clamp((int) Math.round(windowWidth * 0.26), 145, 235);
		int contentX = windowX + border + sidebarWidth + (sidebarCollapsed ? 0 : gap);
		int contentWidth = windowWidth
			- border * 2
			- sidebarWidth
			- (sidebarCollapsed ? 0 : gap);

		return new ClickGuiLayout(
			windowX,
			windowY,
			windowWidth,
			windowHeight,
			windowX + border,
			windowY + border,
			sidebarWidth,
			windowHeight - border * 2,
			contentX,
			windowY + border,
			contentWidth,
			windowHeight - border * 2,
			padding,
			smallGap,
			Math.max(10, (int) Math.round(24 * scale)),
			Math.max(8, (int) Math.round(18 * scale)),
			headerHeight,
			navigationRowHeight,
			moduleRowHeight,
			settingRowHeight,
			Math.max(4, (int) Math.round(6 * scale))
		);
	}

	Rect menuButton() {
		int x = sidebarWidth > 0 ? sidebarX + 16 : contentX + 16;
		return new Rect(x, contentY + 17, 24, 22);
	}

	int sidebarContentTop() {
		return sidebarY + headerHeight + smallGap;
	}

	Rect categoryButton(int index) {
		return new Rect(
			sidebarX + padding,
			sidebarContentTop() + index * (navigationRowHeight + smallGap),
			sidebarWidth - padding * 2,
			navigationRowHeight
		);
	}

	Rect configButton() {
		return new Rect(
			sidebarX + padding,
			sidebarY + sidebarHeight - navigationRowHeight * 2 - smallGap * 2 - padding,
			sidebarWidth - padding * 2,
			navigationRowHeight
		);
	}

	Rect settingsButton() {
		return new Rect(
			sidebarX + padding,
			sidebarY + sidebarHeight - navigationRowHeight - padding,
			sidebarWidth - padding * 2,
			navigationRowHeight
		);
	}

	Rect searchField() {
		int width = Math.clamp(contentWidth / 4, 105, 180);
		return new Rect(
			contentX + contentWidth - width - padding,
			contentY + 14,
			width,
			30
		);
	}

	Rect backButton() {
		return new Rect(contentX + padding, contentY + 15, 16, 18);
	}

	Rect listArea() {
		return new Rect(
			contentX + padding,
			contentY + headerHeight,
			contentWidth - padding * 2,
			contentHeight - headerHeight - padding
		);
	}
}
