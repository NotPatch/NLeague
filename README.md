# NLeague Plugin

![License](https://img.shields.io/badge/License-MIT-green)

Bring a competitive and dynamic atmosphere to your server! NLeague is an advanced, point-based league system where players rank up and down based on their performance. Built for high performance and flexibility, it's the perfect addition to any competitive server.

---

## ✨ Features

* **Dynamic League System:** Players automatically promote or demote through configurable leagues based on the points they collect. Players receive special notifications for promotions and demotions.

* **Advanced Point System:** A system where players gain and lose points through specific actions (like player kills, etc.).

* **Point Multiplier (Boost) System:** Spice up the competition by giving players temporary point boosters. The boost timer **only runs while the player is online** and seamlessly resumes where it left off, even after a server restart.

* **High-Performance Database Support:** To prevent performance loss as your server grows, all player data is securely stored in either a **MySQL** or **SQLite** database.

* **Caching Technology:** Frequently accessed player data is kept in-memory using a high-performance cache (Caffeine), minimizing database load and providing instant response times for in-game actions.

* **Comprehensive Admin Tools:** Easily manage player points, leagues, and boosts via commands. All admin commands are also executable from the server **console**.

* **Advanced Player Feedback:** Instantly inform players with **Action Bar** messages, **Title** announcements, and a fully customizable **progress bar**.

* **Fully Customizable:** Nearly every aspect of the plugin, such as leagues, points, messages, and the progress bar, can be easily configured in the `config.yml`.

---

## ⚙️ Installation

1.  Download the latest `NLeague.jar` from the [Releases](https://github.com/YOUR_USERNAME/YOUR_REPOSITORY/releases) page.
2.  Place it in your server's `plugins` folder.
3.  Start the server. The `config.yml` and other files will be generated automatically.
4.  Open `config.yml` to configure your database settings and other preferences.
5.  Restart the server or use the command `/nleague reload`.

---

## 🎮 Commands & Permissions

### Player Commands
| Command | Description |
| :--- | :--- |
| `/league` | Displays your current league, points, and progress. |
| `/leagues` | Displays all leagues. |

### Admin Commands
> All admin commands require the permission `nleague.admin`.

* `/nleague addpoint <player> <amount>` - Adds a specific amount of points to a player.
* `/nleague removepoint <player> <amount>` - Removes a specific amount of points from a player.
* `/nleague setpoint <player> <amount>` - Directly sets a player's points to a specific value.
* `/nleague setleague <player> <league_id>` - Manually assigns a player to a specific league.
* `/nleague setboost <player> <multiplier> <duration_hours>` - Gives a player a point booster for a specified number of hours.
* `/nleague reload` - Reloads the plugin's configuration files.

---

## 🔧 Configuration

All plugin settings can be managed from the `plugins/NLeague/config.yml` file. The main sections you can configure are:

* **`database`**: Choose between `MySQL` or `SQLite` and enter your connection details.
* **`leagues`**: Define your league IDs, display names, required points, and promotion commands.
* **`messages`**: Customize all user-facing messages, titles, and action bar text.
* **`progress-bar`**: Configure the length, symbols, and colors of the progress bar.

---

## 📜 License

This project is licensed under the MIT License - see the `LICENSE` file for details.
