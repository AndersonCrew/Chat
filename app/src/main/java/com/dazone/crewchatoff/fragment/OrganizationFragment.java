package com.dazone.crewchatoff.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dazone.crewchatoff.Class.TreeParent;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.Class.TreeView;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.IGetListDepart;
import com.dazone.crewchatoff.interfaces.OnOrganizationSelectedEvent;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.Tree.Org_tree;
import com.dazone.crewchatoff.utils.Prefs;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class OrganizationFragment extends BaseFragment implements IGetListDepart, OnOrganizationSelectedEvent {
    private String TAG="OrganizationFragment";
    private LinearLayout ln_container;
    private List<TreeUserDTO> list = new ArrayList<>();
    private ArrayList<TreeUserDTO> selectedPersonList = new ArrayList<>();
    private ArrayList<TreeUserDTOTemp> listTemp = new ArrayList<>();
    //private long task = -10;
    private TreeUserDTO dto = null;
    private String treeUser = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization, container, false);
        ln_container = rootView.findViewById(R.id.container);
        treeUser = new Prefs().getStringValue(Statics.ORANGE, "");
        if (TextUtils.isEmpty(treeUser)) {
            Log.d(TAG, "URL_GET_DEPARTMENT 6");
        } else {
            new Loading().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return rootView;
    }

    private void setupView(TreeUserDTO root) {
        if (root == null)
            return;
        TreeView tree = new TreeParent(getContext(), root, this);
        tree.addToView(ln_container);
        tree.setOnSelectedEvent(this);
    }

    public void convertData(List<TreeUserDTO> treeUserDTOs) {
        if (treeUserDTOs != null && treeUserDTOs.size() != 0) {
            for (TreeUserDTO dto : treeUserDTOs) {
                if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                    list.add(dto);
                    convertData(dto.getSubordinates());
                } else {
                    list.add(dto);
                }
            }
        }
    }

    @Override
    public void onGetListDepartSuccess(ArrayList<TreeUserDTO> treeUserDTOs) {
        Loading loading = new Loading(treeUserDTOs);
        loading.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onGetListDepartFail(ErrorDto dto) {

    }

    @Override
    public void onOrganizationCheck(boolean isCheck, TreeUserDTO personData) {
        int indexOf = selectedPersonList.indexOf(personData);
        if (indexOf != -1) {
            if (!isCheck) {
                selectedPersonList.remove(indexOf);
                unCheckParentData(personData);
            } else {
                selectedPersonList.get(indexOf).setIsCheck(true);
            }
        } else {
            if (isCheck) {
                if (personData.getType() == 2)
                    selectedPersonList.add(personData);
            }
        }
    }

    private void unCheckParentData(TreeUserDTO personData) {

        TreeUserDTO needRemovePerson = null;
        for (TreeUserDTO selectedPerson : selectedPersonList) {
            if (personData.getType() == 2 && selectedPerson.getType() != 2 && selectedPerson.getId() == personData.getId()) {
                needRemovePerson = selectedPerson;
                break;
            } else if (personData.getType() != 2 && selectedPerson.getType() != 2 && selectedPerson.getId() == personData.getParent()) {
                needRemovePerson = selectedPerson;
                break;
            }
        }
        if (needRemovePerson != null) {
            selectedPersonList.remove(needRemovePerson);
            if (needRemovePerson.getParent() > 0) {
                unCheckParentData(needRemovePerson);
            }
        }
    }

    public class Loading extends AsyncTask<Void, Void, Void> {
        List<TreeUserDTO> treeUserDTOs;
        ProgressDialog progressDialog;

        public Loading(List<TreeUserDTO> treeUserDTOs) {
            this.treeUserDTOs = treeUserDTOs;
        }

        public Loading() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (treeUserDTOs != null) {
                convertData(treeUserDTOs);

                for (TreeUserDTO treeUserDTO : list) {
                    if (treeUserDTO.getSubordinates() != null && treeUserDTO.getSubordinates().size() > 0) {
                        treeUserDTO.setSubordinates(null);
                    }
                }

                for (TreeUserDTOTemp treeUserDTOTemp : listTemp) {
                    TreeUserDTO treeUserDTO = new TreeUserDTO(treeUserDTOTemp.getName(), treeUserDTOTemp.getNameEN(), treeUserDTOTemp.getCellPhone(), treeUserDTOTemp.getAvatarUrl(), treeUserDTOTemp.getPosition(), treeUserDTOTemp.getType(), treeUserDTOTemp.getStatus(), treeUserDTOTemp.getUserNo(), treeUserDTOTemp.getDepartNo());
                    list.add(treeUserDTO);
                }

                try {
                    dto = Org_tree.buildTree(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                dto = new Gson().fromJson(treeUser, TreeUserDTO.class);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getActivity(), R.style.StyledDialog);
            progressDialog.setMessage(getString(R.string.loading_title));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            if (dto != null) {
                setupView(dto);
            }
        }
    }
}