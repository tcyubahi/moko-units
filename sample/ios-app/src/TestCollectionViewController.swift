/*
* Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
*/

import UIKit
import MultiPlatformLibrary
import MultiPlatformLibraryUnits

class TestCollectionViewController: UIViewController {
    
    @IBOutlet private var collectionView: UICollectionView!
    private var dataSource: UICollectionViewDataSource!

    override func viewDidLoad() {
        super.viewDidLoad()

        let testing = TestingCollection(unitFactory: self)

        let dataSource = FlatUnitCollectionViewDataSource()
        dataSource.setup(for: collectionView)

        let units = testing.getUnits() as! [UIAnyCellUnitProtocol]
        dataSource.units = units

        self.dataSource = dataSource
    }
}

extension UICollectionViewCell: Reusable {
    public static func xibName() -> String { return String(describing: self) }
    public static func reusableIdentifier() -> String { return String(describing: self) }
}

extension TestCollectionViewController: TestingCollectionCollectionUnitFactory {
    func createSimpleUnit(id: Int64, title: String, itemData: ItemData?) -> CollectionUnitItem {
        // without R.swift
        return UICollectionViewCellUnit<SimpleCollectionCell>(
            data: SimpleCollectionCell.CellModel(id: id, title: title),
            reuseId: SimpleCollectionCell.reusableIdentifier(),
            nibName: SimpleCollectionCell.xibName(),
            configurator: nil
        )
        // with R.swift
//        return UICollectionViewCellUnit<SimpleCollectionCell>(
//            data: SimpleCollectionCell.CellModel(id: id, title: title),
//            reusable: R.nib.simpleCollectionCell,
//            configurator: nil
//        )
    }
}