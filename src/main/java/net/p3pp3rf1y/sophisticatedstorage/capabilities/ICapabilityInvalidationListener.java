package net.p3pp3rf1y.sophisticatedstorage.capabilities;

/**
 * Stand-in for NeoForge's {@code ICapabilityInvalidationListener}: callback notified when block capabilities at a
 * tracked position may have changed.
 */
@FunctionalInterface
public interface ICapabilityInvalidationListener {
	/**
	 * @return {@code true} if the listener remains valid and should stay registered, {@code false} to remove it
	 */
	boolean onInvalidate();
}
