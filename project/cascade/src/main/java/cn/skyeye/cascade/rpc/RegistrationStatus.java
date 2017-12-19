package cn.skyeye.cascade.rpc;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/19 15:41
 */
public enum RegistrationStatus {
    success{
        @Override
        public int statusID() {
            return 1;
        }
    }, regist_wait{
        @Override
        public int statusID() {
            return -1;
        }
    } , refuse{
        @Override
        public int statusID() {
            return 0;
        }
    }, delete{
        @Override
        public int statusID() {
            return 3;
        }
    };

    public abstract int statusID();

    public static RegistrationStatus getRegistrationStatus(String status){
        if(status != null){
            switch (status.toLowerCase()){
                case "success"     : return success;
                case "regist_wait" : return regist_wait;
                case "refuse"      : return refuse;
                case "delete"      : return delete;
            }
        }
        return null;
    }
}
