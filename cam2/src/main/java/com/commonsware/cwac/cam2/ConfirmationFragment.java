/***
 Copyright (c) 2015 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfirmationFragment extends Fragment {
  private static final String ARG_NORMALIZE_ORIENTATION=
    "normalizeOrientation";
  private static final String SENSOR_VALUE ="sensorValue";
  private Float quality;
  private int retakeCount = 0;

  public interface Contract {
    void completeRequest(ImageContext imageContext, boolean isOK);
    void retakePicture();
  }

  private TextView imageText;
  private TextView sensorText;
  private Button retryBtn;
  private ImageView iv;
  private ImageContext imageContext;

  public static ConfirmationFragment newInstance(boolean normalizeOrientation) {
    ConfirmationFragment result=new ConfirmationFragment();
    Bundle args=new Bundle();

    args.putBoolean(ARG_NORMALIZE_ORIENTATION, normalizeOrientation);
    result.setArguments(args);

    return(result);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @Override
  public void onAttach(Activity activity) {
    if (!(activity instanceof Contract)) {
      throw new IllegalStateException("Hosting activity must implement Contract interface");
    }

    super.onAttach(activity);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.confirmation_fragment,container,false);
    iv = (ImageView)view.findViewById(R.id.captured_image);
    sensorText = (TextView) view.findViewById(R.id.sensor_text);
    imageText = (TextView) view.findViewById(R.id.image_text);
    retryBtn = (Button)view.findViewById(R.id.retry);
    retryBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getActivity().getActionBar().show();
        retryBtn.setVisibility(View.GONE);
        imageText.setVisibility(View.GONE);
        getContract().retakePicture();
      }
    });
    if (imageContext!=null) {
      loadImage(quality);
    }
    return view;
  }

  @Override
  public void onHiddenChanged(boolean isHidden) {
    super.onHiddenChanged(isHidden);

    if (!isHidden) {
      ActionBar ab=getActivity().getActionBar();

      if (ab==null) {
        throw new IllegalStateException("CameraActivity confirmation requires an action bar!");
      }
      else {
        ab.setBackgroundDrawable(getActivity()
            .getResources()
            .getDrawable(R.drawable.cwac_cam2_action_bar_bg_translucent));
        ab.setTitle("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          ab.setDisplayHomeAsUpEnabled(false);
          ab.setHomeAsUpIndicator(R.drawable.cwac_cam2_ic_close_white);
        }
        else {
          ab.setIcon(R.drawable.cwac_cam2_ic_close_white);
          ab.setDisplayShowHomeEnabled(false);
          ab.setHomeButtonEnabled(false);
        }
      }
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.cwac_cam2_confirm, menu);
    MenuItem ok = menu.findItem(R.id.cwac_cam2_ok);
    ok.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getContract().completeRequest(imageContext, true);
      }
    });
    MenuItem retry = menu.findItem(R.id.cwac_cam2_retry);
    retry.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getContract().retakePicture();
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId()==android.R.id.home) {
      getContract().completeRequest(imageContext, false);
    }
    else if (item.getItemId()==R.id.cwac_cam2_ok) {
      getContract().completeRequest(imageContext, true);
    }
    else if (item.getItemId()==R.id.cwac_cam2_retry) {
      getContract().retakePicture();
    }
    else {
      return(super.onOptionsItemSelected(item));
    }

    return(true);
  }

  public void setImage(ImageContext imageContext, Float quality) {
    this.imageContext=imageContext;
    this.quality=quality;
    int sensorValue = getActivity().getIntent().getIntExtra(SENSOR_VALUE,0);
    System.out.println("SensorValue: "+sensorValue);
    sensorText.setText(""+sensorValue);
    if(sensorValue<70 && sensorValue>0) {
      if (retakeCount <2) {
        retakeCount++;
        getActivity().getActionBar().hide();
        retryBtn.setVisibility(View.VISIBLE);
        imageText.setVisibility(View.VISIBLE);
        imageText.setText("Dim light. Please take again in brighter light.");
      } else {
        imageText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
      }
    }
    if (iv!=null) {
      loadImage(quality);
    }
  }

  private Contract getContract() {
    return((Contract)getActivity());
  }

  private void loadImage(Float quality) {
    iv.setImageBitmap(imageContext.buildPreviewThumbnail(getActivity(),
      quality, getArguments().getBoolean(ARG_NORMALIZE_ORIENTATION)));
  }
}
