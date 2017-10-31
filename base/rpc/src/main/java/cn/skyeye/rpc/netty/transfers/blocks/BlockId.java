package cn.skyeye.rpc.netty.transfers.blocks;

import cn.skyeye.rpc.netty.transfers.exceptions.UnrecognizedBlockId;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 12:56
 */
public abstract class BlockId {
    protected String name;
    public String getName() {
        return name;
    }

    public static FileBlockId newFileBlockId(String file, long offset, long length){
        return new FileBlockId(file, offset, length);
    }

    public static BlockId parse(String name) throws UnrecognizedBlockId {
        String[] split = name.split("#");
        if(split.length > 1){
            String type = split[0];
            switch (type){
                case "file":
                    String[] args = split[1].split(",");
                    return new FileBlockId(args[0], Long.parseLong(args[1]), Long.parseLong(args[2]));
                case "data":
                    return new DataBlockId(split[1]);
                default:
                    throw new UnrecognizedBlockId(name);
            }
        }else{
            throw new UnrecognizedBlockId(name);
        }
    }

    public static DataBlockId newDataBlockId(String id){
        return new DataBlockId(id);
    }

    public boolean isFileBlockId(){
        return this instanceof FileBlockId;
    }

    public FileBlockId asFileBlockId(){
        if(isFileBlockId()){
            return (FileBlockId)this;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isDataBlockId(){
        return this instanceof DataBlockId;
    }

    public static class FileBlockId extends BlockId{

        private String file;
        private long offset;
        private long length;

        public FileBlockId(String file, long offset, long length){
            this.file = file;
            this.offset = offset;
            this.length = length;
            this.name = String.format("file#%s,%s,%s", file, offset, length);
        }

        public String getFile() {
            return file;
        }

        public long getOffset() {
            return offset;
        }

        public long getLength() {
            return length;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("FileBlockId{");
            sb.append("name='").append(name).append('\'');
            sb.append(", file='").append(file).append('\'');
            sb.append(", offset=").append(offset);
            sb.append(", length=").append(length);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FileBlockId that = (FileBlockId) o;

            if (offset != that.offset) return false;
            if (length != that.length) return false;
            return file.equals(that.file);
        }

        @Override
        public int hashCode() {
            int result = file.hashCode();
            result = 31 * result + (int) (offset ^ (offset >>> 32));
            result = 31 * result + (int) (length ^ (length >>> 32));
            return result;
        }
    }

    public static class DataBlockId extends BlockId{

        private String id;

        public DataBlockId(String id){
           this.id = id;
            this.name = String.format("data#%s", id);
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DataBlockId that = (DataBlockId) o;

            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
