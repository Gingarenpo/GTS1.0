package jp.gingarenpo.gingacore.core;

/**
 * バージョン管理する際にどれが大きいのか小さいのか判断するクラス（インスタンスが必要です）
 * @author 銀河連邦
 *
 */
public class Version {

	private int[] version; // バージョン番号左から順番に
	public String root; // そのまま返すためのもの
	private int buildNo = 0; // ビルド番号。ビルド番号に関しては固有のものをつける。

	/**
	 * 1.2.3.4みたいな感じでバージョンを入力
	 * @param version
	 */
	public Version(String version) {
		// 普通に分解しようか
		this.version = new int[version.split("\\.").length];
		for (int i = 0; i < this.version.length; i++) {
			this.version[i] = Integer.valueOf(version.split("\\.")[i].trim());
		}
		this.root = version;
	}

	/**
	 * 1.2.3.4みたいな感じでバージョンを入力し、かつビルド番号を入力するほう。ビルド番号は同一のバージョンの際に比較の材料となる。
	 * ビルド番号は1から始めること。0は受け付けない。ただし特例として、-1とするといかなるビルド番号よりも大きな数値となる。
	 * @param version
	 * @param buildNo
	 */
	public Version(String version, int buildNo) {
		this(version); // ひとまず上記を実行したうえで
		this.buildNo = buildNo;
	}

	/**
	 * このバージョンが指定したバージョンよりも新しければtrue、古いか同じならばfalseを返す。
	 * バージョンが同じ場合はあればビルド番号をもとに確かめる。
	 * @param other
	 * @return
	 */
	public boolean isLatestThan(Version other) {
		// 多いほうに合わせるので
		int count = (this.version.length > other.version.length) ? this.version.length : other.version.length;

		for (int i = 0; i < count; i++) {
			// 左から順に比較していく。足りない分は0とする
			int me = (this.version.length <= i) ? 0 : this.version[i];
			int you = (other.version.length <= i) ? 0 : other.version[i]; // こうなるね


			if (me > you) return true;
			else if (this.buildNo != 0 && other.buildNo != 0) {
				// ビルド番号が存在する場合
				if ((me == you && this.buildNo > other.buildNo) || (me == you && this.buildNo == -1)) return true; // 比較する
			}
		}
		return false;
	}

	/**
	 * もう片方のバージョンと同じかどうかを検証し、同じならtrue、違えばfalseを返す。ビルド番号がある場合、それが違う場合はfalseを返す
	 * ただし、2番目の引数にfalseを指定した場合はビルド番号を無視する。
	 * @param others
	 * @param checkBuildNumber
	 * @return
	 */
	public boolean equals(Object others, boolean checkBuildNumber) {
		// 同じかどうか
		if (!(others instanceof Version)) {
			return false; // そもそもインスタンスが違う
		}

		Version other = (Version) others; // 安心してダウンキャストできる


		int count = (this.version.length > other.version.length) ? this.version.length : other.version.length;

		for (int i = 0; i < count; i++) {
			// 左から順に比較していく。足りない分は0とする
			int me = (this.version.length <= i) ? 0 : this.version[i];
			int you = (other.version.length <= i) ? 0 : other.version[i]; // こうなるね


			if (me != you) return false;
		}

		if (!checkBuildNumber) return true;

		// ビルド番号を確かめる
		return this.buildNo == other.buildNo; // これで比較できるね
	}

	/**
	 * オーバーロードメソッド。引数省略でtrueになります。
	 * @param others
	 * @return
	 */
	public boolean equals(Object others) {
		return this.equals(others, true);
	}

	@Override
	public String toString() {
		return root;
	}
}
