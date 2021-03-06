/**
 * Created by dungvn3000 on 3/19/14.
 */

Ext.define('sunerp.view.setting.CongThucLuongEdit', {
    extend: 'sunerp.view.core.BaseEditView',
    alias: 'widget.congThucLuongEdit',
    title: 'Edit Công thức lương',
    controller: 'sunerp.controller.setting.CongThucLuongEditCtr',
    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                border: false,
                items: [
                    {
                        xtype: 'textfield',
                        anchor: '100%',
                        name: 'name',
                        minLength: 4,
                        readOnly: true,
                        allowBlank: false,
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'numberfield',
                        anchor: '100%',
                        name: 'value',
                        allowBlank: false,
                        fieldLabel: 'Value'
                    },
                    {
                        xtype: 'numberfield',
                        anchor: '100%',
                        name: 'phongBangId',
                        hidden: true
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});