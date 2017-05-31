// automatically generated by the FlatBuffers compiler, do not modify

package thealphalabs.defaultcamera.model;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class BtPictureInfo extends Table {
  public static BtPictureInfo getRootAsBtPictureInfo(ByteBuffer _bb) { return getRootAsBtPictureInfo(_bb, new BtPictureInfo()); }
  public static BtPictureInfo getRootAsBtPictureInfo(ByteBuffer _bb, BtPictureInfo obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public BtPictureInfo __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String fileName() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer fileNameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public int rawImageData(int j) { int o = __offset(6); return o != 0 ? bb.get(__vector(o) + j * 1) & 0xFF : 0; }
  public int rawImageDataLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer rawImageDataAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }

  public static int createBtPictureInfo(FlatBufferBuilder builder,
      int fileNameOffset,
      int rawImageDataOffset) {
    builder.startObject(2);
    BtPictureInfo.addRawImageData(builder, rawImageDataOffset);
    BtPictureInfo.addFileName(builder, fileNameOffset);
    return BtPictureInfo.endBtPictureInfo(builder);
  }

  public static void startBtPictureInfo(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addFileName(FlatBufferBuilder builder, int fileNameOffset) { builder.addOffset(0, fileNameOffset, 0); }
  public static void addRawImageData(FlatBufferBuilder builder, int rawImageDataOffset) { builder.addOffset(1, rawImageDataOffset, 0); }
  public static int createRawImageDataVector(FlatBufferBuilder builder, byte[] data) { builder.startVector(1, data.length, 1); for (int i = data.length - 1; i >= 0; i--) builder.addByte(data[i]); return builder.endVector(); }
  public static void startRawImageDataVector(FlatBufferBuilder builder, int numElems) { builder.startVector(1, numElems, 1); }
  public static int endBtPictureInfo(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishBtPictureInfoBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}
