package liten.tool.bm.transfer;

/**
 * An abstraction over the service that does chunk-by-chunk transfer.
 */
public interface TransferService {

  boolean prepare();

  String transferNext(String startId);

  void complete();
}
