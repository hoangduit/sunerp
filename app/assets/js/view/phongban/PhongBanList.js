/**
 * Created by dungvn3000 on 3/18/14.
 */
Ext.define('sunerp.view.phongban.PhongBanList', {
    extend: 'sunerp.view.core.BaseListView',
    alias: 'widget.phongBanList',
    requires: ['sunerp.controller.phongban.PhongBanListCtr'],
    controller: 'sunerp.controller.phongban.PhongBanListCtr',
    inject: ['phongBanStore'],
    config: {
        phongBanStore: null
    },
    initComponent: function () {
        var me = this;
        me.store = this.getPhongBanStore();
        me.columns = [
            {xtype: 'rownumberer'},
            {header: 'Tên', dataIndex: 'name', flex: 1},
            {
                header: 'Đơn vị',
                dataIndex: 'donVi.name',
                flex: 1
            },
            {
                xtype: 'actioncolumn',
                header: 'Option',
                sortable: false,
                menuDisabled: true,
                items: [this.deleteBtn()]
            }
        ];
        me.callParent(arguments);
    }
});