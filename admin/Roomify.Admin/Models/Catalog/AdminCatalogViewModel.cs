namespace Roomify.Admin.Models.Catalog;

public class AdminCatalogViewModel
{
    public string ActiveTab { get; set; }
        = "property-types";


    public List<AdminPropertyTypeDto> PropertyTypes { get; set; }
        = [];

    public List<AdminAmenityDto> Amenities { get; set; }
        = [];

    public List<AdminUniversityDto> Universities { get; set; }
        = [];

    public List<ProvinceDto> Provinces { get; set; }
        = [];


    public string? ErrorMessage { get; set; }

    public string? Search { get; set; }

    public string? Status { get; set; }


    // ============================================================
    // PROPERTY TYPES
    // ============================================================

    public int TotalPropertyTypes { get; set; }

    public int ActivePropertyTypes { get; set; }

    public int InactivePropertyTypes { get; set; }


    // ============================================================
    // AMENITIES
    // ============================================================

    public int TotalAmenities { get; set; }

    public int ActiveAmenities { get; set; }

    public int InactiveAmenities { get; set; }


    // ============================================================
    // UNIVERSITIES
    // ============================================================

    public int TotalUniversities { get; set; }

    public int ActiveUniversities { get; set; }

    public int InactiveUniversities { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(
            ErrorMessage);


    public bool IsPropertyTypesTab =>
        string.Equals(
            ActiveTab,
            "property-types",
            StringComparison.OrdinalIgnoreCase);


    public bool IsAmenitiesTab =>
        string.Equals(
            ActiveTab,
            "amenities",
            StringComparison.OrdinalIgnoreCase);


    public bool IsUniversitiesTab =>
        string.Equals(
            ActiveTab,
            "universities",
            StringComparison.OrdinalIgnoreCase);


    public bool IsEmpty =>
        !HasError &&
        (
            IsPropertyTypesTab
                ? PropertyTypes.Count == 0

                : IsAmenitiesTab
                    ? Amenities.Count == 0

                    : Universities.Count == 0
        );
}