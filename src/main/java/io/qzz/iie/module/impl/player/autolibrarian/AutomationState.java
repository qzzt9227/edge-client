package io.qzz.iie.module.impl.player.autolibrarian;

/**
 * 自动刷新流程的离散状态。
 */
enum AutomationState {
	IDLE("client.status.auto_librarian.idle"),
	ROTATING("client.status.auto_librarian.rotating"),
	WAIT_BEFORE_PLACE("client.status.auto_librarian.before_place"),
	WAIT_PLACE_CONFIRM("client.status.auto_librarian.place_confirm"),
	WAIT_PROFESSION("client.status.auto_librarian.wait_profession"),
	WAIT_BEFORE_OPEN("client.status.auto_librarian.before_trade"),
	WAIT_TRADE_SCREEN("client.status.auto_librarian.opening_trade"),
	WAIT_TRADE_DATA("client.status.auto_librarian.reading_trade"),
	WAIT_BEFORE_BREAK("client.status.auto_librarian.before_break"),
	BREAKING("client.status.auto_librarian.breaking"),
	WAIT_BEFORE_RECYCLE("client.status.auto_librarian.before_recycle"),
	WAIT_RECYCLE_DROP("client.status.auto_librarian.wait_recycle_drop"),
	MOVING_TO_RECYCLE_DROP("client.status.auto_librarian.moving_to_recycle_drop"),
	RETURNING_FROM_RECYCLE("client.status.auto_librarian.returning_from_recycle"),
	WAIT_AFTER_BREAK("client.status.auto_librarian.after_break"),
	WAIT_UNEMPLOYED("client.status.auto_librarian.wait_unemployed");

	final String translationKey;

	AutomationState(String translationKey) {
		this.translationKey = translationKey;
	}
}
