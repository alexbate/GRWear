package uk.co.alexbate.grwear;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuGridPagerAdapter extends FragmentGridPagerAdapter {
    private JSONObject json;
    private JSONObject json1;
    private JSONObject json2;

    public MenuGridPagerAdapter(String rd, FragmentManager fm) {
        super(fm);
        try {
            json = new JSONObject(rd);
            JSONArray jsonArray = json.getJSONArray("menus");
            jsonArray = standardiseJSON(jsonArray);
            json1 = jsonArray.getJSONObject(0);
            json2 = jsonArray.getJSONObject(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private JSONArray standardiseJSON(JSONArray array) throws JSONException {
        for (int i=0; i<array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            if (!obj.has("lunch")) {
                obj.put("lunch", new JSONArray());
            }

            if (!obj.has("dinner")) {
                obj.put("dinner", new JSONArray());
            }
        }
        return array;
    }

    @Override
    public Fragment getFragment(int i, int i2) {
        JSONObject today;

        if (i > 1) {
            today = json2;
        } else {
            today = json1;
        }

        if (i2==0) {
            String meal = "";
            switch(i % 2) {
                case 0: meal="Lunch";
                        break;
                case 1: meal="Dinner";
                        break;
            }
            try {
                return CardFragment.create(meal, today.getString("date"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            JSONArray meals = null;
            String description = "";
            JSONObject thisMeal = null;
            try {
                if (i % 2 == 0) {
                    meals = today.getJSONArray("lunch");
                } else {
                    meals = today.getJSONArray("dinner");
                }
                thisMeal = meals.getJSONObject(i2 - 1);
                description = "Item " + Integer.toString(i2);
                description += " - £" + thisMeal.getJSONArray("price").getString(0);
                description += "/£" + thisMeal.getJSONArray("price").getString(1);

            } catch (JSONException e) {
                //Conference period, not dinner
                description = "Item " + Integer.toString(i2);
                if (i!=1) {
                    description += " - £5.50/£7.50";
                } else {
                    description += " - £3.75";

                }
            }

            try {
                return CardFragment.create(description, thisMeal.getString("item"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    @Override
    public int getRowCount() {
        return json1.length() + json2.length() - 2;
    }

    @Override
    public int getColumnCount(int i) {
        int r;
        try {
            switch (i) {
                case (0):
                    r = json1.getJSONArray("lunch").length();
                    break;
                case (1):
                    r = json1.getJSONArray("dinner").length();
                    break;
                case (2):
                    r = json2.getJSONArray("lunch").length();
                    break;
                case (3):
                    r = json2.getJSONArray("dinner").length();
                    break;
                default:
                    r = 0;
            }
            r++; //Add title card
            return r;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
