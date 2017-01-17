package liten.tool.bm.transfer;

/**
 * An abstraction over the service that does chunk-by-chunk transfer.
 */
public interface TransferService {

  boolean prepare();

  Long transferNext(Long startId);

  void complete();
}
