package io.qzz.iie.ui.message;

import io.qzz.iie.api.message.MessageBoxApi;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * 保存有限数量的消息，并生成与帧率无关的淡入淡出快照。
 */
public final class MessageBoxManager implements MessageBoxApi {
	public static final int DEFAULT_CAPACITY = 5;
	private static final long FADE_NANOS = Duration.ofMillis(200).toNanos();

	private final LongSupplier nanoClock;
	private final int capacity;
	private final Deque<Entry> entries = new ArrayDeque<>();

	public MessageBoxManager() {
		this(System::nanoTime, DEFAULT_CAPACITY);
	}

	public MessageBoxManager(LongSupplier nanoClock, int capacity) {
		this.nanoClock = Objects.requireNonNull(nanoClock, "nanoClock");
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity must be positive");
		}
		this.capacity = capacity;
	}

	@Override
	public synchronized void show(Component message, Duration duration) {
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(duration, "duration");
		long durationNanos;
		try {
			durationNanos = duration.toNanos();
		} catch (ArithmeticException overflow) {
			throw new IllegalArgumentException("duration is too large", overflow);
		}
		if (durationNanos <= 0) {
			throw new IllegalArgumentException("duration must be positive");
		}
		long createdNanos = nanoClock.getAsLong();
		long expiresNanos;
		try {
			expiresNanos = Math.addExact(createdNanos, durationNanos);
		} catch (ArithmeticException overflow) {
			throw new IllegalArgumentException("duration exceeds the clock range", overflow);
		}
		while (entries.size() >= capacity) {
			entries.removeFirst();
		}
		entries.addLast(new Entry(message.copy(), createdNanos, expiresNanos));
	}

	public synchronized List<MessageBoxSnapshot> snapshots() {
		long now = nanoClock.getAsLong();
		entries.removeIf(entry -> now >= entry.expiresNanos());
		List<MessageBoxSnapshot> snapshots = new ArrayList<>(entries.size());
		for (Entry entry : entries) {
			snapshots.add(new MessageBoxSnapshot(entry.message(), visibility(entry, now)));
		}
		return List.copyOf(snapshots);
	}

	private static double visibility(Entry entry, long now) {
		double fadeIn = (now - entry.createdNanos()) / (double) FADE_NANOS;
		double fadeOut = (entry.expiresNanos() - now) / (double) FADE_NANOS;
		return Math.clamp(Math.min(fadeIn, fadeOut), 0.0, 1.0);
	}

	private record Entry(Component message, long createdNanos, long expiresNanos) {
	}
}
