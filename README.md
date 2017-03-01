# Android App showcasing Fusion Tables POIs on Google Maps
This repo contains an Android App that queries a Fusion Table and displayes its locations as markers on Google Maps.


- [About Fusion Tables](https://support.google.com/fusiontables/answer/2571232) 
- [Fusion Table used in this example](https://fusiontables.google.com/DataSource?docid=1774o_WcrqSQlepLXlz1kgH_01NpCJ-6OyId9Pm1J)
- [Stackoverflow question and answer related to this example](http://stackoverflow.com/questions/41912999/fusion-tables-v2-pois-on-google-maps-android-example)

## Project setup ##

- [Download and install Android Studio](https://developer.android.com/studio/index.html)
- Create a project in the [Google API Console](https://console.developers.google.com/)
- Enable the [Google Maps Android API](https://console.developers.google.com/apis/api/maps_android_backend/)
- Enable the [Fusion Tables API](https://console.developers.google.com/apis/api/fusiontables/)
- From [API Manager Credentials](https://console.developers.google.com/apis/credentials) > click on "create credentials" > "API key" > copy and paste the key in the `google_maps_api.xml` files, there is one for debug, one for release:
	- `app\src\debug\res\values`
	- `app\src\release\res\values`
- From [IAM & Admin Service Accounts](https://console.developers.google.com/iam-admin/serviceaccounts/) > click on "create service account" > for "Role" select: "Project", "Service Account Actor" > enter a name and an account ID > click on "Furnish a new private key" > click on "Create" > your browser should download a json file containing the private key > rename it to `service_account_credentials.json` and copy it to `app/res/raw/` (create a subdirectory called "raw")
- If you didn't check the checkbox to create the key, you can still click on the 2 dots on the righter side of the service account and click "Create key" > the browser will download the json file at that moment

If you followed these instructions properly you should be able to open the project in Android Studio and debug it.

Enjoy and feel free to create issues if you find bugs in this example, so I can keep it updated. 