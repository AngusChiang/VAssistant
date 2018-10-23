/*
 * Copyright (C) 2018 Light Team Software
 *
 * This file is part of ModPE IDE.
 *
 * ModPE IDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ModPE IDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cn.vove7.rhino.processor.language


import cn.vove7.common.interfaces.VApi
import cn.vove7.common.interfaces.VApi.Companion.androRuntimeFuncs
import cn.vove7.common.interfaces.VApi.Companion.appFunctions
import cn.vove7.common.interfaces.VApi.Companion.executorFunctions
import cn.vove7.common.interfaces.VApi.Companion.finderFuns
import cn.vove7.common.interfaces.VApi.Companion.globalFuns
import cn.vove7.common.interfaces.VApi.Companion.httpFunctions
import cn.vove7.common.interfaces.VApi.Companion.keywordss
import cn.vove7.common.interfaces.VApi.Companion.myApiName
import cn.vove7.common.interfaces.VApi.Companion.runtimeFunctions
import cn.vove7.common.interfaces.VApi.Companion.spFuncs
import cn.vove7.common.interfaces.VApi.Companion.systemFuncs
import cn.vove7.common.interfaces.VApi.Companion.utilFuns
import cn.vove7.common.interfaces.VApi.Companion.viewNodeFunctions
import cn.vove7.common.utils.ArrayUtil
import java.util.regex.Pattern

class MyJsLanguageWithApi : Language(), VApi {

    override fun getSyntaxNumbers(): Pattern {
        return SYNTAX_NUMBERS
    }

    override fun getSyntaxSymbols(): Pattern {
        return SYNTAX_SYMBOLS
    }

    override fun getSyntaxBrackets(): Pattern {
        return SYNTAX_BRACKETS
    }

    override fun getSyntaxKeywords(): Pattern {
        return SYNTAX_KEYWORDS
    }

    override fun getSyntaxMethods(): Pattern {
        return SYNTAX_METHODS
    }

    override fun getSyntaxStrings(): Pattern {
        return SYNTAX_STRINGS
    }

    override fun getSyntaxComments(): Pattern {
        return SYNTAX_COMMENTS
    }

    override fun getLanguageBrackets(): CharArray {
        return LANGUAGE_BRACKETS
    }

    override fun getAllCompletions(): Array<String> {
        return ALL_KEYWORDS
    }

    companion object {


        private val SYNTAX_NUMBERS = Pattern.compile("(\\b(\\d*[.]?\\d+)\\b)")

        private val SYNTAX_SYMBOLS = Pattern.compile(
                "(!|\\+|-|\\*|<|>|=|\\?|\\||:|%|&)")

        private val SYNTAX_BRACKETS = Pattern.compile("(\\(|\\)|\\{|\\}|\\[|\\])")

        private val SYNTAX_KEYWORDS = Pattern.compile(
                "(?<=\\b)((break)|(continue)|(else)|(for)|(function)|(if)|(in)|(new)" +
                        "|(this)|(var)|(while)|(return)|(case)|(catch)|(of)|(typeof)" +
                        "|(const)|(default)|(do)|(switch)|(try)|(null)|(true)" +
                        "|(false)|(eval)|(let))(?=\\b)") //Слова без CASE_INSENSITIVE

        private val SYNTAX_METHODS = Pattern.compile(
                "(?<=(function) )(\\w+)", Pattern.CASE_INSENSITIVE)

        private val SYNTAX_STRINGS = Pattern.compile("\"(.*?)\"|'(.*?)'")

        private val SYNTAX_COMMENTS = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*")

        private val LANGUAGE_BRACKETS = charArrayOf('{', '[', '(', '}', ']', ')') //do not change

        /**
         * Слова для автопродолжения кода.
         */

//region a
        //private static final String[] BLOCK_KEYWORDS = new String[]{
        //        "defineBlock", "defineLiquidBlock", "getAllBlockIds", "getDestroyTime",
        //        "getFriction", "setShape", "getRenderType", "getTextureCoords", "setColor",
        //        "setDestroyTime", "setExplosionResistance", "setFriction", "setRedstoneConsumer",
        //        "setLightLevel", "setLightOpacity", "setRenderLayer", "setRenderType"
        //};
        //private static final String[] ENTITY_KEYWORDS = new String[]{
        //        "getAll", "getAnimalAge", "getArmor", "getArmorCustomName", "getArmorDamage",
        //        "getEntityTypeId", "getExtraData", "getHealth", "getItemEntityCount",
        //        "getItemEntityData", "getItemEntityId", "getMaxHealth", "getMobSkin", "getNameTag",
        //        "getPitch()", "getRenderType", "getRider", "getRiding", "getTarget", "getUniqueId",
        //        "getVelX()", "getVelY()", "getVelZ()", "getYaw()", "isSneaking()", "remove",
        //        "removeAllEffects", "removeEffect", "rideAnimal", "setArmor", "setArmorCustomName",
        //        "setCape", "setCollisionSize", "setExtraData", "setFireTicks", "setHealth",
        //        "setImmobile", "setMaxHealth", "setMobSkin", "setNameTag", "setPosition",
        //        "setPositionRelative", "setCarriedItem", "setRenderType", "setRot", "setSneaking",
        //        "setTarget", "setVelX", "setVelY", "setVelZ", "spawnMob", "addEffect"
        //};
        //private static final String[] ITEM_KEYWORDS = new String[]{
        //        "getMaxDamage", "getMaxStackSize", "defineArmor", "defineThrowable",
        //        "getCustomThrowableRenderType", "addCraftRecipe", "setMaxDamage", "addFurnaceRecipe",
        //        "getName", "getTextureCoords", "getUseAnimation", "internalNameToId", "isValidItem",
        //        "setCategory", "setEnchantType", "addShapedRecipe", "setHandEquipped", "setProperties",
        //        "setStackedByData", "setUseAnimation", "translatedNameToId"
        //};
        //private static final String[] LEVEL_KEYWORDS = new String[]{
        //        "biomeIdToName", "canSeeSky", "setSpawnerTypeId", "destroyBlock", "explode",
        //        "getAddress", "getBiome", "getBiomeName", "getBrightness", "getGameMode",
        //        "getGrassColor", "getDifficulty", "setDifficulty", "getTile", "getData", "getTime",
        //        "getWorldDir()", "getWorldName", "setGameMode", "setGrassColor", "getLightningLevel()",
        //        "getRainLevel()", "setNightMode", "setSpawn", "setTile", "setTime", "spawnMob",
        //        "getSignText", "setSignText", "addParticle", "playSound", "playSoundEnt",
        //        "setBlockExtraData", "dropItem", "getChestSlot", "getChestSlotCount",
        //        "getChestSlotData", "setChestSlot", "setChestSlotCustomName", "setSpawnerEntityType",
        //        "setLightningLevel", "setRainLevel", "getFurnaceSlot", "getFurnaceSlotCount",
        //        "getFurnaceSlotData", "setFurnaceSlot"
        //};
        //private static final String[] MODPE_KEYWORDS = new String[]{
        //        "getOS()", "dumpVtable", "getI18n", "getBytesFromTexturePack", "getLanguage",
        //        "getMinecraftVersion", "langEdit", /*"leaveGame"*/"openInputStreamFromTexturePack",
        //        "overrideTexture", "readData", "removeData", "saveData", "resetFov", "resetImages",
        //        "setFoodItem", "setFov", "setGameSpeed", "setItem", "showTipMessage",
        //        "setUiRenderDebug", "takeScreenshot", "setGuiBlocks", "setItems", "setTerrain",
        //        "selectLevel"
        //};
        //private static final String[] PLAYER_KEYWORDS = new String[]{
        //        "addExp", "addItemInventory", "addItemCreativeInv", "canFly()", "clearInventorySlot",
        //        "enchant", "getEnchantments", "getArmorSlot", "getArmorSlotDamage", /*"getCarriedItem"*/
        //        "getCarriedItemCount", "getCarriedItemData", "getDimension", "getEntity",
        //        "getExhaustion", "getExp", "getHunger", "getInventorySlot", "getInventorySlotCount",
        //        "getInventorySlotData", "getItemCustomName", "setInventorySlot", "getLevel", "setLevel",
        //        "setSaturation", "setSelectedSlotId", "setItemCustomName", "getName",
        //        "getPointedBlockId()", "getPointedBlockData()", "getPointedBlockSide()",
        //        "getPointedBlockX()", "getPointedBlockY()", "getPointedBlockZ()", "getPointedEntity()",
        //        "getPointedVecX()", "getPointedVecY()", "getPointedVecZ()", "getSaturation", "getScore",
        //        "getSelectedSlotId()", /*"getX", "getY", "getZ",*/ "isFlying()", "setCanFly",
        //        "setFlying", /*"setHealth"*/"setArmorSlot", "setExhaustion", "setExp",
        //        /*"addItemCreativeInv",*/ "setHunger", "isPlayer()"
        //};
        //private static final String[] SERVER_KEYWORDS = new String[]{
        //        /*"getAddress",*/ "getAllPlayerNames()", "getAllPlayers()", "getPort()", "joinServer",
        //        "sendChat"
        //};
        //private static final String[] HOOKS_KEYWORDS = new String[]{
        //        "useItem", /*"destroyBlock",*/ "newLevel", "procCmd", "selectLevelHook",
        //        /*"leaveGame",*/ "attackHook", "modTick", "eatHook", "explodeHook", "deathHook",
        //        "entityAddedHook", "entityRemovedHook", "entityHurtHook", "projectileHitEntityHook",
        //        "playerAddExpHook", "playerExpLevelChangeHook", "redstoneUpdateHook",
        //        "startDestroyBlock", "continueDestroyBlock", "blockEventHook", "levelEventHook",
        //        "serverMessageReceiveHook", "screenChangeHook", "chatReceiveHook", "chatHook"
        //};
        // endregion a
        private val JS_KEYWORDS = arrayOf("break", "else", "new",
                "var", "case", "finally", "return", "void", "catch", "for",
                "switch", "while", "continue", "function", "this", "with", "default",
                "if", "throw", "delete", "in", "try", "do", "instranceof", "typeof"
        )
        //private static final String[] GLOBAL_KEYWORDS = new String[]{
        //        "clientMessage", "getPlayerX()", "getPlayerY()", "getPlayerZ()", "getPlayerEnt()"
        //};

        /**
         * Соединение всех массивов в один. Этот массив и будет использоваться для
         * получения слов в редакторе.
         */
        private val ALL_KEYWORDS = ArrayUtil.merge(arrayOf(keywordss, myApiName,
                appFunctions, runtimeFunctions, executorFunctions, viewNodeFunctions,
                finderFuns, globalFuns, systemFuncs, utilFuns, spFuncs, JS_KEYWORDS, httpFunctions,
                androRuntimeFuncs))
    }

}
