package net.simplyvanilla.simplyrank.data;

/**
 * <p>Represents the two possible outcomes of an IO operation in the context of this plugin</p>
 *
 * @param <T> The success type (e.g. PlayerData)
 * @param <E> (The error type (e.g. IOException)
 */
public interface IOCallback<T, E> {

    /**
     * <p>Called if the load operation has succeeded</p>
     *
     * @param data the data
     */
    void success(T data);

    /**
     * <p>Called if the laod operation has failed</p>
     *
     * @param error the error
     */
    void error(E error);
    

}
