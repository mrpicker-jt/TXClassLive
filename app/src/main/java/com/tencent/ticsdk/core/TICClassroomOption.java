package com.tencent.ticsdk.core;

/**
 * 课堂参数配置
 */
public class TICClassroomOption{
    public final static int ROLE_TEACHER = 0;
    public final static int ROLE_STUDENT = 1;
    /**
     * 房间ID，由业务维护
     */
    public int classId = -1;

    /**
     * ntp服务器
     * @brief 进房成功后从ntp服务器获取服务器时间戳作为白板课后录制的对时信息，默认使用time1.cloud.tencent.com。为保证对时信息的高度一致，建议各端使用一样的对时地址。
     **/
    public String ntpServer = "time1.cloud.tencent.com";

    public int getClassId() {
        return classId;
    }

    public TICClassroomOption setClassId(int classId) {
        this.classId = classId;
        return this;
    }

    @Override
    public String toString() {
        return "TICClassroomOption{" +
                "classId=" + classId +
                ",ntpServer=" + ntpServer +
                '}';
    }
}
