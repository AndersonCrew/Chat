package com.dazone.crewchatoff.activity.chatting

import android.os.Bundle
import com.dazone.crewchatoff.Class.ChatInputView
import com.dazone.crewchatoff.R
import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import com.dazone.crewchatoff.HTTPs.HttpRequest
import com.dazone.crewchatoff.activity.*
import com.dazone.crewchatoff.activity.ChattingActivity.userNos
import com.dazone.crewchatoff.activity.base.BaseActivity
import com.dazone.crewchatoff.constant.Statics
import com.dazone.crewchatoff.database.ChatMessageDBHelper
import com.dazone.crewchatoff.database.ChatRoomDBHelper
import com.dazone.crewchatoff.dto.ChattingDto
import com.dazone.crewchatoff.dto.ErrorDto
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack
import com.dazone.crewchatoff.utils.*
import com.nononsenseapps.filepicker.FilePickerActivity
import kotlinx.android.synthetic.main.activity_chating2.*


class Chating2Activity : BaseActivity() {
    private lateinit var inputView: ChatInputView
    private lateinit var viewModel: ChatttingViewModel
    private var roomNo: Long? = null
    private var myId: Long? = null
    private var chattingDTO: ChattingDto? = null

    companion object {
        fun toActivity(context: Context, tempDto: ChattingDto) {
            val intent = Intent(context, Chating2Activity::class.java)
            intent.putExtra(Constant.KEY_INTENT_ROOM_DTO, tempDto)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chating2)
        getDataIntent()
        initViews()
        initViewModels()
        initControls()
    }

    private fun getDataIntent() {

        intent?.getSerializableExtra(Constant.KEY_INTENT_ROOM_DTO)?.let {
            chattingDTO = it as ChattingDto
        }

        chattingDTO?.roomNo?.let {
            roomNo = it
        }

        myId = Utils.getCurrentId().toLong()

    }

    private fun initViews() {
        inputView = ChatInputView(this)
        // llInput.addView(inputView as View)
    }

    private fun initControls() {
        icBack.setOnClickListener { onBackPressed() }
        icMore.setOnClickListener { showFilterPopup(icMore) }
    }

    private fun initViewModels() {
        viewModel = ViewModelProviders.of(this).get(ChatttingViewModel::class.java)

        chattingDTO?.let {
            viewModel.getHeader(it)
        }

        viewModel.title?.observe(this, Observer {
            tvTitle?.text = it
        })

        viewModel.status?.observe(this, Observer {
            tvParticipants?.text = it
        })

        /*Leave Group*/
        viewModel.leaveGroupSuccess?.observe(this, Observer {
            it?.let { success ->
                dismissProgressDialog()
                if (success) {
                    val intent = Intent(this@Chating2Activity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        })
    }

    private fun showFilterPopup(v: View) {
        val popup = PopupMenu(this, v)
        // Inflate the menu from xml
        popup.menuInflater.inflate(R.menu.menu_in_chatting, popup.menu)
        val menu = popup.menu

        if (CrewChatApplication.getInstance().prefs.ddsServer.contains(Statics.chat_jw_group_co_kr)) {
            menu.findItem(R.id.menu_send_file).isVisible = false
        }

        menu.findItem(R.id.menu_left_group).isVisible = chattingDTO?.listTreeUser?.size ?: 0 > 1

        // Setup menu item selection
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_list_chat -> {
                    startChatList()
                    true
                }

                R.id.menu_add_chat -> {
                    startAddMember()
                    true
                }

                R.id.menu_left_group -> {
                    showProgressDialog()
                    viewModel.leaveGroup()
                    true
                }

                R.id.menu_send_file -> {
                    if (PermissionUtil.checkPermissions(this, PermissionUtil.permissionsStorage)) {
                        startSelectFile()
                    } else {
                        PermissionUtil.requestPermissions(this, Config.REQUEST_STORAGE_PERMISSION, PermissionUtil.permissionsStorage)
                    }
                    true
                }

                R.id.menu_close -> {
                    finish()
                    true
                }

                R.id.menu_room_rename -> {
                    startRenameRoom()
                    true
                }
                R.id.menu_iv_file_box -> {
                    startImageBox()
                    true
                }
                R.id.menu_attach_file_box -> {
                    startAttachFile()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun startChatList() {
        val intent = Intent(this, RoomUserInformationActivity::class.java)
        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo)
        intent.putExtra("userNos", userNos)
        intent.putExtra("roomTitle", title)
        startActivity(intent)
    }

    private fun startAddMember() {
        val intent = Intent(this, InviteUserActivity::class.java)
        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, roomNo)
        intent.putExtra(Constant.KEY_INTENT_COUNT_MEMBER, userNos)
        intent.putExtra(Constant.KEY_INTENT_ROOM_TITLE, title)
        startActivityForResult(intent, Statics.ADD_USER_SELECT)
    }

    private fun startSelectFile() {
        val i = Intent(ChattingActivity.Instance, FilePickerActivity::class.java)
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true)
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE)
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().path)
        startActivityForResult(i, Statics.FILE_PICKER_SELECT)
    }

    private fun startRenameRoom() {
        val roomInfo = Bundle()
        val unwrappedRoomNo = roomNo?.toInt()?: return
        val unwrappedTitle = viewModel.title?.value?: return
        roomInfo.putInt(Statics.ROOM_NO, unwrappedRoomNo)
        roomInfo.putString(Statics.ROOM_TITLE, unwrappedTitle)

        val intent = Intent(this, RenameRoomActivity::class.java)
        intent.putExtras(roomInfo)
        startActivityForResult(intent, Statics.RENAME_ROOM)
    }

    private fun startImageBox() {
        val intent = Intent(this, ImageFileBoxActivity::class.java)
        intent.putExtra(Statics.ROOM_NO, roomNo)
        startActivity(intent)
    }

    private fun startAttachFile() {
        val intent = Intent(this, AttachFileBoxActivity::class.java)
        intent.putExtra(Statics.ROOM_NO, roomNo)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Config.REQUEST_STORAGE_PERMISSION -> {
                startSelectFile()
            }

            Statics.ADD_USER_SELECT -> {
                //TODO Add user to group
            }

            Statics.RENAME_ROOM -> {
                val roomTitle = data?.getStringExtra(Statics.ROOM_TITLE)
                viewModel.title?.value = roomTitle

                val roomNo = data?.getIntExtra(Statics.ROOM_NO, 0)?: return
                val prefs = CrewChatApplication.getInstance().prefs
                prefs.roomName = roomTitle
                prefs.putRoomId(roomNo)
                // Start new thread to update local database
                Thread(Runnable { ChatRoomDBHelper.updateChatRoom(roomNo.toLong(), roomTitle) }).start()
            }
        }
    }
}
