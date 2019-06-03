package com.tencent.ticsdk.trtcdemo.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.teduboard.TEduBoardController;
import com.tencent.ticsdk.trtcdemo.R;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;


public class TICMenuDialog extends Dialog implements View.OnClickListener {

    private final static String TAG                                     = "TICMenuDialog";
    final static String CLOUD_FILE = "cloud:";
    final static String LOCAL_FILE = "local:";

    //涂鸭工具集映射
    static class ToolType {
        ToolType(String name, int type) {
            this.name = name;
            this.type = type;
        }
        String name;
        int type;
    }

    static class KValue {
        KValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String key;
        String value;
    }

    //普通ppt文件，放在云端
    private final static String [] FILE_URL = {
            "http://e4fb-edu-1400127140-1257240443.cos.ap-shanghai.myqcloud.com/TIC/WEB/1557368577909_3.%E6%88%AA%E4%B8%80%E4%B8%AA%E5%87%A0%E4%BD%95%E4%BD%93.ppt"
    };

    //PPT转码后的H5文件，
    private final static String [] H5FILE_URL = {
            "https://test04-1257240443.cos.ap-shanghai.myqcloud.com/2019-05-08-15-38-54/index.html",
            "https://test04-1257240443.cos.ap-shanghai.myqcloud.com/2019-05-08-15-18-25-0/index.html",
            "http://test04-1257240443.cos.ap-shanghai.myqcloud.com/9m4e2mr0ui3e8a215n4g/index.html"
    };

    //PPT转码后的H5文件，
    private final static KValue [] H5_BK_URL = {
            new KValue("https://www.qq.com/", "QQ"),
//            new KValue("https://cloud.tencent.com", "腾讯云"),
            new KValue("http://b.hiphotos.baidu.com/image/h%3D300/sign=92afee66fd36afc3110c39658318eb85/908fa0ec08fa513db777cf78376d55fbb3fbd9b3.jpg", "美图"),
    };


    final ToolType [] mToolMap = {
            new ToolType("鼠标", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_MOUSE),
            new ToolType("铅笔", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_PEN),
            new ToolType("橡皮", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_ERASER),
            new ToolType("直线", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_LINE),
            new ToolType("激光教鞭", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_LASER),
            new ToolType("空心椭圆", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_OVAL),
            new ToolType("空心矩形", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_RECT),
            new ToolType("实心椭圆", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_OVAL_SOLID),
            new ToolType("实心矩形", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_RECT_SOLID),
            new ToolType("点选", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_POINT_SELECT),
            new ToolType("框选", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_RECT_SELECT),
            new ToolType("文本", TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_TEXT)
    };

    final ToolType [] ColorMap = {
            new ToolType("白", 0xFFFFFF),
            new ToolType("红", 0xFF0000),
            new ToolType("橙", 0xFFA500),
            new ToolType("黄", 0xFFFF00),
            new ToolType("绿", 0x00FF00),
            new ToolType("青", 0x00FFFF),
            new ToolType("蓝", 0x0000ff),
            new ToolType("紫", 0x800080),
    };

    final ToolType [] TextStyle = {
            new ToolType("常规", TEduBoardController.TEduBoardTextStyle.TEDU_BOARD_TEXT_STYLE_NORMAL),
            new ToolType("粗体", TEduBoardController.TEduBoardTextStyle.TEDU_BOARD_TEXT_STYLE_BOLD),
            new ToolType("斜体", TEduBoardController.TEduBoardTextStyle.TEDU_BOARD_TEXT_STYLE_ITALIC),
            new ToolType("粗斜体", TEduBoardController.TEduBoardTextStyle.TEDU_BOARD_TEXT_STYLE_BOLD_ITALIC),
    };



    public interface IMoreListener {
        //TRTC
        void onEnableAudio(boolean bEnableAudio);
        void onEnableCamera(boolean bEnableCamera);
        void onSwitchCamera(boolean bFrontCamera);

        //Board(涂鸭操作)
        void onSetDrawEnable(boolean SetDrawEnable);
        void onSetToolType(int type);
        void onBrushThin(int size);
        void onSetTextSize(int size);

        void onSetBrushColor(int color);
        void onSetTextColor(int color);
        void onSetTextStyle(int style);
        void onSetTextFamily(String family);
        void onSetBackgroundColore(int color);
        void onSetBackgroundImage(String path);
        void onSetBackgroundH5(String url);


        void onUndo();
        void onRedo();
        void onClear();
        void onReset();

        //Board(白板操作)
        void onAddBoard(String id);
        void onDeleteBoard(String boardId);
        void onGotoBoard(String boardId);
        void onPrevStep();
        void onNextStep();
        void onPrevBoard();
        void onNextBoard();

        //Board(文件操作)
        void onAddFile(String id);
        void onAddH5File(String url);
        void onDeleteFile(String boardId);
        void onGotoFile(String boardId);

    }

    //显示时的值
    public static class SettingCacheData {
        //trtc
        boolean AudioEnable;
        boolean CameraEnable;
        boolean CameraFront;

        //board(涂鸭)
        boolean isDrawEnable;
        int ToolType;
        int BrushThin;
        int BrushColor;
        int TextColor;
        int TextStyle;
        String TextFamily;
        int BackgroundColor;
        int GlobalBackgroundColor;
        int TextSize;
        String BackgroundImage;
        boolean canRedo;
        boolean canUndo;

        //board(白板操作)
        String currentBoardId;
        List<String> boardIds;

        //board(文件操作)
        String currentFileId;
        List<TEduBoardController.TEduBoardFileInfo> files;
    };


    WeakReference<IMoreListener> mMoreListener;
    SettingCacheData mSettingData;

    //
    ArrayAdapter<String> boardArray;

    public TICMenuDialog(Context context, IMoreListener listener) {
        super(context, R.style.room_more_dlg);
        mMoreListener = new WeakReference<>(listener);
        mSettingData = new SettingCacheData();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_setting);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //initView(mSettingData);
    }

    @Override
    protected void onStart() {
        initView(mSettingData);
        super.onStart();
    }

    public void show(SettingCacheData settingData) {
        mSettingData = settingData;
        show();
    }

    private void initView(SettingCacheData settingData) {
        //返回
        findViewById(R.id.btn_back).setOnClickListener(this);

        //---------TRTC----------
        CheckBox audio_enalbe = findViewById(R.id.audio_enable);
        audio_enalbe.setOnClickListener(this);
        audio_enalbe.setChecked(settingData.AudioEnable);

        CheckBox video_enalbe = findViewById(R.id.camera_enalbe);
        video_enalbe.setOnClickListener(this);
        video_enalbe.setChecked(settingData.CameraEnable);

        RadioButton mRbCameraFront   = (RadioButton)findViewById(R.id.camera_front);
        mRbCameraFront.setOnClickListener(this);
        mRbCameraFront.setChecked(settingData.CameraFront);

        RadioButton mRbCameraBack   = (RadioButton)findViewById(R.id.camera_back);
        mRbCameraBack.setOnClickListener(this);
        mRbCameraBack.setChecked(!settingData.CameraFront);

        //---------Board(涂鸭)---------
        CheckBox board_isDrawEnable = findViewById(R.id.board_setDrawEnable);
        board_isDrawEnable.setOnClickListener(this);
        board_isDrawEnable.setChecked(settingData.isDrawEnable);

        int posToolType = 0;
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        for (int i = 0; i < mToolMap.length; i++) {
            adapter.add( mToolMap[i].name);
            if (settingData.ToolType == mToolMap[i].type) {
                posToolType = i;
            }
        }
        Spinner spToolType = (Spinner)findViewById(R.id.sp_SetToolType);
        spToolType.setAdapter(adapter);
        spToolType.setSelection(posToolType, false);
        findViewById(R.id.btn_SetToolType).setOnClickListener(this);


        findViewById(R.id.btn_BrushThin).setOnClickListener(this);
        ((TextView)findViewById(R.id.tv_BrushThin)).setText(String.valueOf(settingData.BrushThin));
        SeekBar sbVideoBitrate = findViewById(R.id.sk_BrushThin);
        sbVideoBitrate.setProgress(settingData.BrushThin);
        sbVideoBitrate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)findViewById(R.id.tv_BrushThin)).setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.btn_SetTextSize).setOnClickListener(this);
        ((TextView)findViewById(R.id.tv_SetTextSize)).setText(String.valueOf(settingData.TextSize));
        SeekBar sk_TextSize = (SeekBar)findViewById(R.id.sk_SetTextSize);
        sk_TextSize.setProgress(settingData.TextSize);
        sk_TextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)findViewById(R.id.tv_SetTextSize)).setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button bt_undo = findViewById(R.id.btn_undo);
        bt_undo.setEnabled(settingData.canUndo);
        bt_undo.setOnClickListener(this);

        Button bt_redo = findViewById(R.id.btn_redo);
        bt_redo.setEnabled(settingData.canRedo);
        bt_redo.setOnClickListener(this);

        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_reset).setOnClickListener(this);

        int pos_brushcolor = 0;
        int pos_textcolor = 0;
        int pos_backgroundcolor = 0;
        ArrayAdapter<String> colors =new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        for (int i = 0; i < ColorMap.length; i++) {
            colors.add(ColorMap[i].name);

            if (ColorMap[i].type == settingData.BrushColor) {
                pos_brushcolor = i;
            }
            if (ColorMap[i].type == settingData.TextColor) {
                pos_textcolor = i;
            }
            if (ColorMap[i].type == settingData.BackgroundColor) {
                pos_backgroundcolor = i;
            }
        }
        Spinner spBruchColor = (Spinner)findViewById(R.id.sp_setBrushColor);
        spBruchColor.setAdapter(colors);
        spBruchColor.setSelection(pos_brushcolor, false);
        findViewById(R.id.btn_setBrushColor).setOnClickListener(this);

        Spinner spTextColor = (Spinner)findViewById(R.id.sp_setTextColor);
        spTextColor.setAdapter(colors);
        spTextColor.setSelection(pos_textcolor, false);
        findViewById(R.id.btn_setTextColor).setOnClickListener(this);

        int pos_textstyle = 0;
        ArrayAdapter<String> textstyle = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        for (int i = 0; i < TextStyle.length; i++) {
            textstyle.add(TextStyle[i].name);
            if (TextStyle[i].type == settingData.TextStyle) {
                pos_textstyle = i;
            }
        }

        Spinner spTextStyle = (Spinner)findViewById(R.id.sp_setTextStyle);
        spTextStyle.setAdapter(textstyle);
        spTextStyle.setSelection(pos_textstyle, false);
        findViewById(R.id.btn_setTextStyle).setOnClickListener(this);

        ArrayAdapter<String> textfamily = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        if (!TextUtils.isEmpty(settingData.TextFamily)) {
            textfamily.add(settingData.TextFamily);
            Spinner spTextFamily = (Spinner)findViewById(R.id.sp_setTextFamily);
            spTextFamily.setAdapter(textfamily);
            spTextFamily.setSelection(0, false);
        }
        findViewById(R.id.btn_setTextFamily).setOnClickListener(this);


        Spinner spbackgroundColor = (Spinner)findViewById(R.id.sp_setBackgroundColor);
        spbackgroundColor.setAdapter(colors);
        spbackgroundColor.setSelection(pos_backgroundcolor, false);
        findViewById(R.id.btn_setBackgroundColor).setOnClickListener(this);

        ArrayAdapter<String> imgArray =new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        String[] imgs = null;
        try {
            imgs = getContext().getAssets().list("img");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imgs != null) {
            for (String file : imgs) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(file);
                if (extension != null && (extension.equals("jpg") || extension.equals("png") || extension.equals("bmp"))) {
                    imgArray.add(file);
                }
            }
        }
        Spinner backimage = (Spinner)findViewById(R.id.sp_setBackgroundImage);
        backimage.setAdapter(imgArray);
        backimage.setSelection(0, false);
        findViewById(R.id.btn_setBackgroundImage).setOnClickListener(this);


        ArrayAdapter<String> bgh5Array =new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        for (int i = 0; i < H5_BK_URL.length; i++) {
            bgh5Array.add(H5_BK_URL[i].value);
        }
        Spinner bgh5 = (Spinner)findViewById(R.id.sp_setBackgroundH5);
        bgh5.setAdapter(bgh5Array);
        bgh5.setSelection(0, false);
        findViewById(R.id.btn_setBackgroundH5).setOnClickListener(this);


        //---------Board(白板)---------
        findViewById(R.id.btn_prevBoard).setOnClickListener(this);
        findViewById(R.id.btn_nextBoard).setOnClickListener(this);

        findViewById(R.id.btn_prevStep).setOnClickListener(this);
        findViewById(R.id.btn_nextStep).setOnClickListener(this);


        findViewById(R.id.btn_addBoard).setOnClickListener(this);
        findViewById(R.id.btn_deleteBoard).setOnClickListener(this);
        findViewById(R.id.btn_gotoBoard).setOnClickListener(this);

        int pos = 0;
        boardArray = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
        if (settingData.boardIds != null && settingData.boardIds.size() > 0) {
            if (!TextUtils.isEmpty(mSettingData.currentBoardId)) {
                pos = settingData.boardIds.indexOf(mSettingData.currentBoardId);
            }

            boardArray.addAll(settingData.boardIds);
        }
        Spinner deleteboard = (Spinner)findViewById(R.id.sp_deleteBoard);
        deleteboard.setAdapter(boardArray);
        deleteboard.setSelection(pos >= 0 ? pos : 0, false );

        Spinner gotoboard = (Spinner)findViewById(R.id.sp_gotoBoard);
        gotoboard.setAdapter(boardArray);
        gotoboard.setSelection(pos >=0 ? pos : 0, false );


        //---------Board(文件)---------
        findViewById(R.id.btn_addFile).setOnClickListener(this);
        findViewById(R.id.btn_addH5File).setOnClickListener(this);
        findViewById(R.id.btn_deleteFile).setOnClickListener(this);
        findViewById(R.id.btn_switchFile).setOnClickListener(this);

        ArrayAdapter<String> pptArray =new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);

        int ppt_index = 0;
        for (String url : FILE_URL) { //URL 网络文件
            String fileName = CLOUD_FILE + "PPT_URL_" + ppt_index;
            pptArray.add(fileName);
            ppt_index++;
        }

        String[] ppts = null;
        try {
            ppts = getContext().getAssets().list("ppt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ppts != null) { //本地文件
            for (String file : ppts) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(file);
                if (extension != null && (extension.equals("ppt") || extension.equals("pptx") || extension.equals("pdf"))) {
                    pptArray.add(LOCAL_FILE + file);
                }
            }
        }

        Spinner sp_addFile = (Spinner)findViewById(R.id.sp_addFile);
        sp_addFile.setAdapter(pptArray);

        //H5文件
        ArrayAdapter<String> pptH5Array =new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        int h5_index = 0;
        for (String h5url : H5FILE_URL) {
            String fileName = CLOUD_FILE + "H5_URL_" + h5_index;
            pptH5Array.add(fileName);
            h5_index++;
        }
        Spinner sp_addH5File = (Spinner)findViewById(R.id.sp_addH5File);
        sp_addH5File.setAdapter(pptH5Array);


        int pos_file = 0;
        ArrayAdapter<String> filesArray = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_item, android.R.id.text1);
        if (settingData.files != null) {
            int i = 0;
            for(TEduBoardController.TEduBoardFileInfo info : settingData.files) {
                filesArray.add(TextUtils.isEmpty(info.title) ? info.fileId : info.title);
                if (info.fileId.equals(mSettingData.currentFileId)) {
                    pos_file = i;
                }
                i++;
            }
        }

        Spinner deletefile = (Spinner)findViewById(R.id.sp_deleteFile);
        deletefile.setAdapter(filesArray);
        deletefile.setSelection(pos_file >=0 ? pos_file : 0, false);

        Spinner switchfile = (Spinner)findViewById(R.id.sp_switchFile);
        switchfile.setAdapter(filesArray);
        switchfile.setSelection(pos_file >=0 ? pos_file : 0, false);
    }

    @Override
    public void onClick(View v) {
        IMoreListener listener = mMoreListener.get();
        if (listener == null) {
            return;
        }

        int id = v.getId();
        switch (id) {
            case R.id.btn_back:
                dismiss();
                break;

            //TRTC
            case R.id.audio_enable:
                boolean audio_enable = ((CheckBox)v).isChecked();
                listener.onEnableAudio(audio_enable);
                break;

            case R.id.camera_enalbe:
                boolean camera_enalbe = ((CheckBox)v).isChecked();
                listener.onEnableCamera(camera_enalbe);
                break;

            case R.id.camera_front:
                boolean camera_front = ((RadioButton)v).isChecked();
                listener.onSwitchCamera(camera_front);
                break;

            case R.id.camera_back:
                boolean camera_back = ((RadioButton)v).isChecked();
                listener.onSwitchCamera(!camera_back);
                break;

           //Board(涂鸭)
            case R.id.board_setDrawEnable:
                boolean setDrawEnable = ((CheckBox)v).isChecked();
                listener.onSetDrawEnable(setDrawEnable);
                break;

            case R.id.btn_SetToolType:
                int pos = ((Spinner)findViewById(R.id.sp_SetToolType)).getSelectedItemPosition();
                if (pos < mToolMap.length) {
                    int type = mToolMap[pos].type;
                    listener.onSetToolType(type);
                }
                break;

            case R.id.btn_BrushThin:
                int BrushThin = ((SeekBar)findViewById(R.id.sk_BrushThin)).getProgress();
                listener.onBrushThin(BrushThin);
                break;

            case R.id.btn_SetTextSize:
                int SetTextSize = ((SeekBar)findViewById(R.id.sk_SetTextSize)).getProgress();
                    listener.onSetTextSize(SetTextSize);
                break;

            case R.id.btn_undo:
                listener.onUndo();
                break;

            case R.id.btn_redo:
                listener.onRedo();
                break;

            case R.id.btn_clear:
                listener.onClear();
                break;

            case R.id.btn_reset:
                listener.onReset();
                break;

            case R.id.btn_setBrushColor:
                int pos1 = ((Spinner)findViewById(R.id.sp_setBrushColor)).getSelectedItemPosition();
                if (pos1 < ColorMap.length) {
                    int type = ColorMap[pos1].type;
                    listener.onSetBrushColor(type);
                }
                break;

            case R.id.btn_setTextColor:
                int pos2 = ((Spinner)findViewById(R.id.sp_setTextColor)).getSelectedItemPosition();
                if (pos2 < ColorMap.length) {
                    int type = ColorMap[pos2].type;
                    listener.onSetTextColor(type);
                }
                break;

            case R.id.btn_setTextStyle:
                int postext = ((Spinner)findViewById(R.id.sp_setTextStyle)).getSelectedItemPosition();
                if (postext < TextStyle.length) {
                    int type = TextStyle[postext].type;
                    listener.onSetTextStyle(type);
                }
                break;

            case R.id.btn_setTextFamily:
            {
                Object pos4 = ((Spinner)findViewById(R.id.sp_setTextFamily)).getSelectedItem();
                if (pos4 != null && pos4 instanceof String) {
                    final String family = (String)pos4;
                    listener.onSetTextFamily(family);
                }
            }
                break;

            case R.id.btn_setBackgroundColor:
                int pos3 = ((Spinner)findViewById(R.id.sp_setBackgroundColor)).getSelectedItemPosition();
                if (pos3 < ColorMap.length) {
                    int type = ColorMap[pos3].type;
                    listener.onSetBackgroundColore(type);
                }
                break;

            case R.id.btn_setBackgroundImage:
                Object pos4 = ((Spinner)findViewById(R.id.sp_setBackgroundImage)).getSelectedItem();
                if (pos4 != null && pos4 instanceof String) {
                    final String path = "file:///android_asset/img/" + (String)pos4;
                    listener.onSetBackgroundImage(path);
                }
                break;

            case R.id.btn_setBackgroundH5:
                int pos_h5bk = ((Spinner)findViewById(R.id.sp_setBackgroundH5)).getSelectedItemPosition();
                if (pos_h5bk < H5_BK_URL.length) {
                    final String path = H5_BK_URL[pos_h5bk].key;
                    listener.onSetBackgroundH5(path);
                }
                break;


            //Board(白板)
            case R.id.btn_prevBoard:
                listener.onPrevBoard();
                break;

            case R.id.btn_nextBoard:
                listener.onNextBoard();
                break;

            case R.id.btn_prevStep:
                listener.onPrevStep();
                break;

            case R.id.btn_nextStep:
                listener.onNextStep();
                break;

            case R.id.btn_addBoard:
                String boardid0 = "Test_" + ((int)(Math.random() *1000));
                listener.onAddBoard(boardid0);
                break;

            case R.id.btn_deleteBoard:
                {
                    Spinner deleteboard  = findViewById(R.id.sp_deleteBoard);

                    Object objdeleteboard = deleteboard.getSelectedItem();
                    if (objdeleteboard != null && objdeleteboard instanceof String) {
                        String boardid = (String) objdeleteboard;
                        listener.onDeleteBoard(boardid);
//                        boardArray.remove(boardid);
                    }
                }
                break;

            case R.id.btn_gotoBoard:
                Object objgotoboard = ((Spinner)findViewById(R.id.sp_gotoBoard)).getSelectedItem();
                if (objgotoboard != null && objgotoboard instanceof String) {
                    listener.onGotoBoard((String) objgotoboard);
                }
                break;

            //Board(文件)
            case R.id.btn_addFile:
                Object pos5 = ((Spinner)findViewById(R.id.sp_addFile)).getSelectedItem();
                if (pos5 != null && pos5 instanceof String) {
                    String path = "";
                    final String item = (String) pos5;
                    if (item.startsWith(CLOUD_FILE)) {
                        try {
                            int start = (CLOUD_FILE + "PPT_URL_").length();
                            int url_ppt_pos = Integer.valueOf(item.substring(start));
                            if (url_ppt_pos >=0 && url_ppt_pos < FILE_URL.length) {
                                path = FILE_URL[url_ppt_pos];
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else if (item.startsWith(LOCAL_FILE)){
                        path = "file:///android_asset/ppt/" + item.substring(LOCAL_FILE.length());
                    }

                    if (!TextUtils.isEmpty(path)) {
                        listener.onAddFile(path);
                    }
                }
                break;

            case R.id.btn_addH5File:
                int pos_H5 = ((Spinner)findViewById(R.id.sp_addH5File)).getSelectedItemPosition();
                if (pos_H5 >=0 && pos_H5 < H5FILE_URL.length) {
                    final String path = H5FILE_URL[pos_H5];
                    listener.onAddH5File(path);
                }
                break;

            case R.id.btn_deleteFile:
                Object objTitle = ((Spinner)findViewById(R.id.sp_deleteFile)).getSelectedItem();
                if (objTitle != null && objTitle instanceof String) {
                    final String title = (String)objTitle;
                    for (TEduBoardController.TEduBoardFileInfo file : mSettingData.files) {
                        if (title.equals(file.title)) {
                            listener.onDeleteFile(file.fileId);
                            return;
                        }
                    }
                }
                break;

            case R.id.btn_switchFile:
                Object pos7 = ((Spinner)findViewById(R.id.sp_switchFile)).getSelectedItem();
                if (pos7 != null && pos7 instanceof String) {
                    String title = (String)pos7;

                    for (TEduBoardController.TEduBoardFileInfo file : mSettingData.files) {
                        if (title.equals(file.title)) {
                            listener.onGotoFile(file.fileId);
                            return;
                        }
                    }

                    Toast.makeText(getContext(), "file is not exist.", Toast.LENGTH_LONG);
                }
                break;
        }
    }

}
