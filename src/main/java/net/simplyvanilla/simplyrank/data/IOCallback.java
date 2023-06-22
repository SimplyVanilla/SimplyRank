package net.simplyvanilla.simplyrank.data;

/**
 * Represents the two possible outcomes of an IO operation in the context of this plugin
 *
 * @param <T> The success type (e.g. PlayerData)
 * @param <E> (The error type (e.g. IOException)
 */
public interface IOCallback<T, E> {

    /**
     * Called if the load operation has succeeded
     *
     * @param data the data
     */
    void success(T data);

    /**
     * Called if the laod operation has failed
     *
     * @param error the error
     */
    void error(E error);
}
