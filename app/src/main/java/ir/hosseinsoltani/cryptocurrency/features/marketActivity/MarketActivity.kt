package ir.hosseinsoltani.cryptocurrency.features.marketActivity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import ir.hosseinsoltani.cryptocurrency.apiManager.ApiManager
import ir.hosseinsoltani.cryptocurrency.apiManager.model.CoinAboutData
import ir.hosseinsoltani.cryptocurrency.apiManager.model.CoinAboutItem
import ir.hosseinsoltani.cryptocurrency.apiManager.model.CoinsData
import ir.hosseinsoltani.cryptocurrency.databinding.ActivityMarketBinding
import ir.hosseinsoltani.cryptocurrency.features.coinActivity.CoinActivity

// 1. add library
// 2. internet permission
// 3. create interface for our fun
// 4. init Retrofit and ApiService

class MarketActivity : AppCompatActivity(), MarketAdapter.RecyclerCallback {
    lateinit var binding: ActivityMarketBinding
    lateinit var adapter: MarketAdapter
    lateinit var dataNews: ArrayList<Pair<String, String>>
    lateinit var aboutDataMap: MutableMap<String, CoinAboutItem>
    val apiManager = ApiManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.layoutToolbar.toolbar.title = "Crypto Currency"

        firstRunShowDialogWelcome()

        binding.layoutWatchlist.btnShowMore.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.livecoinwatch.com/"))
            startActivity(intent)
        }
        binding.swipeRefreshMain.setOnRefreshListener {

            initUi()

            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshMain.isRefreshing = false
            }, 1500)

        }

        getAboutDataFromAssets()

    }
    override fun onResume() {
        super.onResume()

        initUi()

    }

    private fun initUi() {

        getNewsFromApi()
        getTopCoinsFromApi()

    }

    private fun getNewsFromApi() {

        apiManager.getNews(object : ApiManager.ApiCallback<ArrayList<Pair<String, String>>> {

            override fun onSuccess(data: ArrayList<Pair<String, String>>) {
                dataNews = data
                refreshNews()
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(this@MarketActivity, "error => " + errorMessage, Toast.LENGTH_SHORT)
                    .show()
            }

        })

    }
    private fun refreshNews() {

        val randomAccess = (0..49).random()
        binding.layoutNews.txtNews.text = dataNews[randomAccess].first
        binding.layoutNews.imgNews.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dataNews[randomAccess].second))
            startActivity(intent)
        }
        binding.layoutNews.txtNews.setOnClickListener {
            refreshNews()
        }

    }

    private fun getTopCoinsFromApi() {

        apiManager.getCoinsList(object : ApiManager.ApiCallback<List<CoinsData.Data>> {
            override fun onSuccess(data: List<CoinsData.Data>) {

                showDataInRecycler(data)

            }

            override fun onError(errorMessage: String) {
                Toast.makeText(this@MarketActivity, "error => " + errorMessage, Toast.LENGTH_SHORT)
                    .show()
                Log.v("testLog", errorMessage)
            }
        })

    }
    private fun showDataInRecycler(data: List<CoinsData.Data>) {

        adapter = MarketAdapter(ArrayList(data), this)
        binding.layoutWatchlist.recyclerMain.adapter = adapter
        binding.layoutWatchlist.recyclerMain.layoutManager = LinearLayoutManager(this)

    }
    override fun onCoinItemClicked(dataCoin: CoinsData.Data) {
        val intent = Intent(this, CoinActivity::class.java)

        val bundle = Bundle()
        bundle.putParcelable("bundle1", dataCoin)
        bundle.putParcelable("bundle2", aboutDataMap[dataCoin.coinInfo.name])

        intent.putExtra("bundle", bundle)
        startActivity(intent)
    }

    private fun getAboutDataFromAssets() {

        val fileInString = applicationContext.assets
            .open("currencyinfo.json")
            .bufferedReader()
            .use { it.readText() }

        aboutDataMap = mutableMapOf<String, CoinAboutItem>()

        val gson = Gson()
        val dataAboutAll = gson.fromJson(fileInString, CoinAboutData::class.java)

        dataAboutAll.forEach {
            aboutDataMap[it.currencyName] = CoinAboutItem(
                it.info.web,
                it.info.github,
                it.info.twt,
                it.info.desc,
                it.info.reddit
            )
        }

    }

    private fun firstRunShowDialogWelcome() {

        val shared = getSharedPreferences("mainSharedPref.xml", Context.MODE_PRIVATE)
        val isFirstRun = shared.getBoolean("isFirstRun", true)
        if (isFirstRun) {

            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("Ø§ÛŒÙ† Ø¨Ø±Ù†Ø§Ù…Ù‡ ÛŒÚ©ÛŒ Ø§Ø² Ù¾Ø±ÙˆÚ˜Ù‡ Ù‡Ø§ÛŒ Ø¯Ø§Ø®Ù„ Ø¯ÙˆØ±Ù‡ ÛŒØ§Ù‚ÙˆØª Ø§Ù†Ø¯Ø±ÙˆÛŒØ¯ ÙˆØ¨Ø³Ø§ÛŒØª Ø¯Ø§Ù†ÛŒØ¬Øª Ø§Ø³Øª Ú©Ù‡ Ø¯Ø± Ù‚Ø§Ù„Ø¨ Û±Û° Ø³Ø§Ø¹Øª Ø¯ÙˆØ±Ù‡ Ø¢Ù…ÙˆØ²Ø´ÛŒ Ø¶Ø¨Ø· Ø´Ø¯Ù‡ Ùˆ Ø¢Ù…Ø§Ø¯Ù‡ Ø§Ø³ØªÙØ§Ø¯Ù‡ Ø§Ø³Øª :)" +
                    "\n\n Ø¯ÙˆØ±Ù‡ ÛŒØ§Ù‚ÙˆØª Ø§Ù†Ø¯Ø±ÙˆÛŒØ¯ Ø´Ø§Ù…Ù„ Û´Û° ÙØµÙ„ Ú©Ø§Ù…Ù„ Ø¨Ù‡ Ø²Ø¨Ø§Ù† Ú©Ø§ØªÙ„ÛŒÙ† Ø§Ø³Øª Ú©Ù‡ Ú©Ø§Ù…Ù„Ø§ Ù¾Ø±ÙˆÚ˜Ù‡ Ù…Ø­ÙˆØ± Ø¨ÙˆØ¯Ù‡ Ùˆ Ø§ÛŒÙ† Ù¾Ø±ÙˆÚ˜Ù‡ ÙÙ‚Ø· Ù¾Ø±ÙˆÚ˜Ù‡ ÛŒÚ©ÛŒ Ø§Ø² ÙØµÙˆÙ„ Ø§ÛŒÙ† Ø¯ÙˆØ±Ù‡ Ø§Ø³Øª" +
                    "\n\n Ø¯Ø± Ù¾Ø§ÛŒØ§Ù† Ø¯ÙˆØ±Ù‡ ÛŒÚ© Ù¾Ø±ÙˆÚ˜Ù‡ ÙØ±ÙˆØ´Ú¯Ø§Ù‡ÛŒ Ú©Ø§Ù…Ù„ Ø¨Ù‡ Ù‡Ù…Ø±Ø§Ù‡ Ø¯Ø±Ú¯Ø§Ù‡ Ù¾Ø±Ø¯Ø§Ø®Øª Ù‡Ù… Ø¨Ù‡ Ø²Ø¨Ø§Ù† Ú©Ø§ØªÙ„ÛŒÙ† ØªÙˆØ³Ø¹Ù‡ Ø¯Ø§Ø¯Ù‡ Ø´Ø¯Ù‡ Ø§Ø³Øª" +
                    "\n\n Ø¯Ø± ØµÙˆØ±Øª Ø¹Ù„Ø§Ù‚Ù‡ Ø¨Ù‡ Ø­Ø±ÙÙ‡ Ø§ÛŒ Ø´Ø¯Ù† Ø¯Ø± Ø²Ù…ÛŒÙ†Ù‡ Ø§Ù†Ø¯Ø±ÙˆÛŒØ¯ Ø¨Ù‡ Ù„ÛŒÙ†Ú© Ø²ÛŒØ± Ø³Ø± Ø¨Ø²Ù†ÛŒØ¯ : " +
                    "\n https://dunijet.ir/product/yaghot-android/")
            dialog.show()

            shared.edit().putBoolean("isFirstRun" , false).apply()
        }

    }

}