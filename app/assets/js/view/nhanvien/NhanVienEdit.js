/**
 * Created by dungvn3000 on 3/18/14.
 */
Ext.define('sunerp.view.nhanvien.NhanVienEdit', {
    extend: 'sunerp.view.core.BaseEditView',
    alias: 'widget.nhanVienEdit',
    title: 'Edit NhanVien',
    requires: ['sunerp.controller.nhanvien.NhanVienEditCtr'],
    controller: 'sunerp.controller.nhanvien.NhanVienEditCtr',
    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                border: false,
                items: [
                    {
                        xtype: 'textfield',
                        name: 'firstName',
                        minLength: 4,
                        allowBlank: false,
                        fieldLabel: 'Tên'
                    },
                    {
                        xtype: 'textfield',
                        name: 'lastName',
                        minLength: 4,
                        allowBlank: false,
                        fieldLabel: 'Họ'
                    },
                    {
                        xtype: 'numberfield',
                        name: 'heSoLuong',
                        minValue:0,
                        allowBlank: false,
                        fieldLabel: 'Hệ số lương'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});